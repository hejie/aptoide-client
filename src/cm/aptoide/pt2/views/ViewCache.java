/*
 * ViewCache, part of Aptoide
 * Copyright (C) 2012 Duarte Silveira
 * duarte.silveira@caixamagica.pt
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
package cm.aptoide.pt2.views;

import java.io.File;

import cm.aptoide.pt2.util.Constants;
import cm.aptoide.pt2.util.Md5Handler;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * ViewCache
 *
 * @author dsilveira
 *
 */
public class ViewCache implements Parcelable{

	private long id;
	private String localPath;
	private boolean hasMd5Sum;
	private String md5sum;
	
	private EnumCacheType type;
	

	public ViewCache(EnumCacheType type, long id) {
		this.id = id;
		this.type = type; 
		switch (type) {
			case APK:
				this.localPath = Constants.PATH_CACHE_APKS+id;
				break;
	
			default:
				break;
		}
		this.hasMd5Sum = false;
	}
	
	public ViewCache(EnumCacheType type, long id, String md5sum) {
		this(type, id);
		this.hasMd5Sum = true;
		this.md5sum = md5sum;
	}

	public long getId(){
		return id;
	}

	public String getLocalPath() {
		return localPath;
	}
	
	public File getFile(){
		return new File(localPath);
	}
	
	public long getFileLength(){
		return getFile().length();
	}
	
	public boolean hasMd5Sum(){
		return hasMd5Sum;
	}
	
	public void setMd5Sum(String md5sum){
		this.md5sum = md5sum;
	}

	public String getMd5sum() {
		return md5sum;
	}
	
	public EnumCacheType getType(){
		return type;
	}
	
	
	
	public boolean isCached(){
		File icon = new File(this.localPath);
		if(icon.exists()){
			Log.d("Aptoide-ManagerCache", "already cached: "+this.localPath);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * clearCache, if it exists remove it
	 * 
	 * @param cache
	 */
	public void clearCache(){
		File file = new File(this.localPath);
		if(file.exists()){
			file.delete();
			Log.d("Aptoide-ManagerCache", "deleted: "+this.localPath);
		}
	}
	
	/**
	 * chechMd5, checks if the file md5Sum corresponds to the expected signature
	 * 
	 * @return true if checks out ok, or if there is no stored signature
	 */
	public boolean checkMd5(){
		if(!hasMd5Sum){
			return true;
		}else{
			File file = new File(this.localPath);
			String actualMd5Sum = Md5Handler.md5Calc(file);
			if(this.md5sum.equalsIgnoreCase(actualMd5Sum)){
				Log.d("Aptoide-ManagerCache", "md5Check OK!");
				return true;
			}else{
				Log.d("Aptoide-ManagerCache",md5sum+ " VS " + actualMd5Sum);
				return false;
			}
		}
	}

	/**
	 * hashCode, unsafe cast from long (theoretically the id which is the db's auto-increment id will not overflow integer in a realistic scenario)
	 */
	@Override
	public int hashCode() {
		return (int) id;
	}


	@Override
	public boolean equals(Object object) {
		if(object instanceof ViewDownloadManagement){
			ViewDownloadManagement cache = (ViewDownloadManagement) object;
			if(cache.hashCode() == this.hashCode()){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "ViewCache:  localPath: "+localPath+" md5sum: "+md5sum;
	}

	public void clean(){
		this.localPath = null;
		this.hasMd5Sum = false;
		this.md5sum = null;
	}
	
	public void reuse(String localPath) {
		this.localPath = localPath;
		this.hasMd5Sum = false;
	}
	
	public void reuse(String localPath, String md5sum) {
		reuse(localPath);
		this.hasMd5Sum = true;
		this.md5sum = md5sum;
	}
	
	
	
	// Parcelable stuff //
	
	
	public static final Parcelable.Creator<ViewCache> CREATOR = new Parcelable.Creator<ViewCache>() {
		public ViewCache createFromParcel(Parcel in) {
			return new ViewCache(in);
		}

		public ViewCache[] newArray(int size) {
			return new ViewCache[size];
		}
	};

	/** 
	 * we're annoyingly forced to create this even if we clearly don't need it,
	 *  so we just use the default return 0
	 *  
	 *  @return 0
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	protected ViewCache(Parcel in){
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(localPath);
		out.writeValue(hasMd5Sum);
		if(hasMd5Sum){
			out.writeString(md5sum);
		}
	}

	public void readFromParcel(Parcel in) {
		this.localPath = in.readString();
		this.hasMd5Sum = (Boolean) in.readValue(null);
		if(hasMd5Sum){
			this.md5sum = in.readString();
		}
	}
}