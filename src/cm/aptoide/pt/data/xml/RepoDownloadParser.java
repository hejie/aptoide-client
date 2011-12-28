/**
 * RepoDownloadParser, 	auxiliary class to Aptoide's ServiceData
 * Copyright (C) 2011 Duarte Silveira
 * duarte.silveira@caixamagica.pt
 * 
 * derivative work of previous Aptoide's RssHandler with
 * Copyright (C) 2009  Roberto Jacinto
 * roberto.jacinto@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package cm.aptoide.pt.data.xml;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import cm.aptoide.pt.data.Constants;
import cm.aptoide.pt.data.downloads.ViewDownloadInfo;
import cm.aptoide.pt.data.model.ViewAppDownloadInfo;
import cm.aptoide.pt.data.model.ViewIconInfo;

/**
 * RepoDownloadParser, handles Download Repo xml Sax parsing
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class RepoDownloadParser extends DefaultHandler{
	private ManagerXml managerXml = null;
	
	private ViewXmlParse parseInfo;
	private ViewAppDownloadInfo downloadInfo;	
	private ArrayList<ViewAppDownloadInfo> downloadsInfo = new ArrayList<ViewAppDownloadInfo>(Constants.APPLICATIONS_IN_EACH_INSERT);
	private ArrayList<ArrayList<ViewAppDownloadInfo>> downloadsInfoInsertStack = new ArrayList<ArrayList<ViewAppDownloadInfo>>(2);
	
	private EnumXmlTagsDownload tag = EnumXmlTagsDownload.apklst;
	private HashMap<String, EnumXmlTagsDownload> tagMap = new HashMap<String, EnumXmlTagsDownload>();
	
	private int appHashid = 0;
	private int appFullHashid = 0;
	private int parsedAppsNumber = 0;
	
	private StringBuilder tagContentBuilder;
	
		
	public RepoDownloadParser(ManagerXml managerXml, ViewXmlParse parseInfo, int appHashid){
		this.managerXml = managerXml;
		this.parseInfo = parseInfo;
		this.appHashid = appHashid;
		
		for (EnumXmlTagsDownload tag : EnumXmlTagsDownload.values()) {
			tagMap.put(tag.name(), tag);
		}
	}
	
	@Override
	public void characters(final char[] chars, final int start, final int length) throws SAXException {
		super.characters(chars, start, length);
		
		tagContentBuilder.append(new String(chars, start, length).trim());
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		switch (tag) {
			case apphashid:
				appFullHashid = (Integer.parseInt(tagContentBuilder.toString())+"|"+parseInfo.getRepository().getHashid()).hashCode();
				break;
				
			case path:
				String appRemotePathTail = tagContentBuilder.toString();
				downloadInfo = new ViewAppDownloadInfo(appRemotePathTail, appFullHashid);
				break;
				
			case md5h:
				downloadInfo.setMd5hash(tagContentBuilder.toString());
				break;
				
			case sz:
				downloadInfo.setSize(Integer.parseInt(tagContentBuilder.toString()));
				break;
				
			default:
				break;
		}
		
		if(localName.trim().equals(EnumXmlTagsIcon.pkg.toString())){
			if(parsedAppsNumber >= Constants.APPLICATIONS_IN_EACH_INSERT){
				parsedAppsNumber = 0;
				downloadsInfoInsertStack.add(downloadsInfo);

				Log.d("Aptoide-RepoDownloadParser", "bucket full, inserting apps: "+downloadsInfo.size());
				try{
					new Thread(){
						public void run(){
							this.setPriority(Thread.MAX_PRIORITY);
							final ArrayList<ViewAppDownloadInfo> downloadsInfoInserting = downloadsInfoInsertStack.remove(Constants.FIRST_ELEMENT);
							
							managerXml.getManagerDatabase().insertDownloadsInfo(downloadsInfoInserting);
						}
					}.start();
	
				} catch(Exception e){
					/** this should never happen */
					//TODO handle exception
					e.printStackTrace();
				}
				
				downloadsInfo = new ArrayList<ViewAppDownloadInfo>(Constants.APPLICATIONS_IN_EACH_INSERT);
			}
			parsedAppsNumber++;
			parseInfo.getNotification().incrementProgress(1);
			
			downloadsInfo.add(downloadInfo);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		tagContentBuilder = new StringBuilder();
		tag = tagMap.get(localName.trim());
	}
	
	
	
	
	@Override
	public void startDocument() throws SAXException {	//TODO refacto Logs
		Log.d("Aptoide-RepoDownloadHandler","Started parsing XML from " + parseInfo.getRepository() + " ...");
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		Log.d("Aptoide-RepoIconHandler","Done parsing XML from " + parseInfo.getRepository() + " ...");
		
		if(!downloadsInfo.isEmpty()){
			Log.d("Aptoide-RepoDownloadParser", "bucket not empty, apps: "+downloadsInfo.size());
			downloadsInfoInsertStack.add(downloadsInfo);
		}

		Log.d("Aptoide-RepoInfoParser", "buckets: "+downloadsInfoInsertStack.size());
		while(!downloadsInfoInsertStack.isEmpty()){
			managerXml.getManagerDatabase().insertDownloadsInfo(downloadsInfoInsertStack.remove(Constants.FIRST_ELEMENT));			
		}
		
		managerXml.parsingRepoDownloadFinished(parseInfo.getRepository(), appHashid);
		super.endDocument();
	}


}
