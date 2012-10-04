package cm.aptoide.pt2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import cm.aptoide.pt2.contentloaders.ImageLoader;
import cm.aptoide.pt2.util.Base64;
import cm.aptoide.pt2.util.Md5Handler;
import cm.aptoide.pt2.views.ViewApk;

public class ItemBasedApks {

	private ViewApk apk;
	private Context context;
	private View container;
	private Database db;
	
	
	public ItemBasedApks(Context context, ViewApk apk) {
		this.context = context;
		this.apk=apk;
		this.db = Database.getInstance(context);
	}

	public void getItems(View container) {
		this.container=container;
		new ItemLoader().execute(apk.getApkid());
		new ItemBasedParser().execute(apk.getApkid());
		
		
	}
	
	public class ItemLoader extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>>{

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				String... params) {
			return db.getItemBasedApks(params[0].hashCode());
		}
		
		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			super.onPostExecute(result);
			loadItems(result);
		}
		
	}
	public class ItemBasedParser extends AsyncTask<String, Void, ArrayList<HashMap<String, String>> >{

		private String xmlpath = Environment.getExternalStorageDirectory()+"/.aptoide/itembasedapks.xml";
		private String url = "http://www.aptoide.com/webservices/listItemBasedApks/%s/10/xml";
		
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
			String apkid = params[0];

			File f = new File(xmlpath);
			InputStream in;
			try {
				in = getInputStream(new URL(String.format(url,apkid)), null, null);
//				int i = 0;
//				while (f.exists()) {
//					f = new File(xmlpath + i++);
//				}
				FileOutputStream out = new FileOutputStream(f);

				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				out.close();
				
				String md5hash = Md5Handler.md5Calc(f);
				int parentHashCode = apk.getApkid().hashCode(); 
				if(!md5hash.equals(db.getItemBasedApksHash(apk.getApkid().hashCode()))){
					db.insertItemBasedApkHash(md5hash,parentHashCode);
					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser parser = factory.newSAXParser();
					parser.parse(f, new ItemBasedApkHandler(db, apk));
					return db.getItemBasedApks(parentHashCode);
				}
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		};
		
		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> values) {
			super.onPostExecute(values);
			if(values!=null ){
				loadItems(values);
			}
			
		}
	}
	
	public InputStream getInputStream(URL url,String username, String password) throws IOException {
		URLConnection connection = url.openConnection();
		if(username!=null && password!=null){
			String basicAuth = "Basic " + new String(Base64.encode((username+":"+password).getBytes(),Base64.NO_WRAP ));
			connection.setRequestProperty ("Authorization", basicAuth);
		}
		BufferedInputStream bis = new BufferedInputStream(
				connection.getInputStream(), 8 * 1024);
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		System.out.println("Query:" + url.toString());
		return bis;

	}

	private void loadItems(ArrayList<HashMap<String, String>> values) {
					ImageLoader imageLoader = new ImageLoader(context,db);
						LinearLayout ll = (LinearLayout) container; 
						ll.removeAllViews();
				        LinearLayout llAlso = new LinearLayout(context);
				        llAlso.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				        llAlso.setOrientation(LinearLayout.HORIZONTAL);
				        for (int i = 0; i!=values.size(); i++) {
				            RelativeLayout txtSamItem = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.griditem, null);
				           	((TextView) txtSamItem.findViewById(R.id.name)).setText(values.get(i).get("name"));
				           	imageLoader.DisplayImage(-1, values.get(i).get("icon"), (ImageView)txtSamItem.findViewById(R.id.icon), context,false);
				           	float stars = 0f;
				           	try{
				           		stars = Float.parseFloat(values.get(i).get("rating"));
				           	}catch (Exception e) {
				           		stars = 0f;
							}
		//		           	((RatingBar) txtSamItem.findViewById(R.id.rating)).setRating(stars);
				            txtSamItem.setPadding(10, 0, 0, 0);
		//		            txtSamItem.setTag(values.get(i).get("id"));
				            txtSamItem.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 100, 1));
		//		            txtSamItem.setOnClickListener(featuredListener);
		
				            txtSamItem.measure(0, 0);
				            
				            if (i%2==0) {
				                ll.addView(llAlso);
		
				                llAlso = new LinearLayout(context);
				                llAlso.setLayoutParams(new LayoutParams(
				                        LayoutParams.FILL_PARENT,
				                        100));
				                llAlso.setOrientation(LinearLayout.HORIZONTAL);
				                llAlso.addView(txtSamItem);
				            } else {
				                llAlso.addView(txtSamItem);
				            }
				        }
		
				        ll.addView(llAlso);
	}
	
	
	
}