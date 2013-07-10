package cm.aptoide.pt.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import cm.aptoide.pt.ApplicationAptoide;
import cm.aptoide.pt.R;
import cm.aptoide.pt.download.*;
import cm.aptoide.pt.download.event.DownloadStatusEvent;
import cm.aptoide.pt.download.state.EnumState;
import cm.aptoide.pt.events.BusProvider;
import cm.aptoide.pt.util.Constants;
import cm.aptoide.pt.views.ViewApk;
import com.squareup.otto.Subscribe;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 08-07-2013
 * Time: 12:00
 * To change this template use File | Settings | File Templates.
 */
public class ServiceManagerDownload extends Service {
    private static final String DEFAULT_APK_DESTINATION = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.aptoide/";
    private static final String OBB_DESTINATION = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/obb/";
    private NotificationManager managerNotification;
    private Collection<DownloadInfo> ongoingDownloads;



    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();  //To change body of implemented methods use File | Settings | File Templates.
    }

    HashMap<Integer, DownloadInfo> downloads = new HashMap<Integer, DownloadInfo>();

    public Collection<DownloadInfo> getDownloads() {
        return downloads.values();
    }

    public ArrayList<DownloadInfo> getNotOngoingDownloads() {
        ArrayList<DownloadInfo> list = new ArrayList<DownloadInfo>();

        for(DownloadInfo downloadInfo: getDownloads()){
            if(downloadInfo.getStatusState().getEnumState().equals(EnumState.COMPLETE) ){
                list.add(downloadInfo);
            }
        }

        return list;
    }

    public class LocalBinder extends Binder {

        public ServiceManagerDownload getService(){

            BusProvider.getInstance().register(ServiceManagerDownload.this);

            return ServiceManagerDownload.this;
        }

    }



    public DownloadInfo getDownload(ViewApk apk){

        if(downloads.get(apk.getAppHashId())!=null){
            return downloads.get(apk.getAppHashId());
        }else{
            return new DownloadInfo(apk.getAppHashId(), apk);
        }

    }

    public void startDownload(DownloadInfo download, ViewApk apk){
        downloads.put(apk.getAppHashId(), download);
        ArrayList<DownloadModel> downloadList = new ArrayList<DownloadModel>();
        download.setDownloadExecutor(new DownloadExecutorImpl());
        DownloadModel apkDownload = new DownloadModel(apk.getPath(), DEFAULT_APK_DESTINATION + apk.getApkid() + "." +apk.getMd5()+".apk", apk.getMd5());
        apkDownload.setAutoExecute(true);
        downloadList.add(apkDownload);
        if(apk.getMainObbUrl()!=null){
            DownloadModel mainObbDownload = new DownloadModel(apk.getMainObbUrl(), OBB_DESTINATION + apk.getApkid() + "/" +apk.getMainObbFileName(), apk.getMainObbMd5());
            downloadList.add(mainObbDownload);
            if(apk.getPatchObbUrl()!=null){
                DownloadModel patchObbDownload = new DownloadModel(apk.getPatchObbUrl(), OBB_DESTINATION + apk.getApkid() + "/" +apk.getPatchObbFileName(), apk.getPatchObbMd5());
                downloadList.add(patchObbDownload);
            }
        }
        download.setFilesToDownload(downloadList);
        download.download();

    }

    @Subscribe public void removeDownload(DownloadRemoveEvent id){
        BusProvider.getInstance().post(downloads.remove(id.getId()));
        BusProvider.getInstance().post(new DownloadStatusEvent());
    }

    @Subscribe public void updateDownload(DownloadInfo id){
        ongoingDownloads = getOngoingDownloads();

        if(!ongoingDownloads.isEmpty()){
            setNotification();
        }else{
            dismissNotification();
        }

    }

    private synchronized void dismissNotification(){
        try {
            managerNotification.cancel(this.hashCode());
        } catch (Exception e) { }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        DownloadManager.INSTANCE.removeAllActiveDownloads();
        dismissNotification();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        dismissNotification();
        super.onDestroy();
    }

    private void setNotification() {

        managerNotification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        Intent onClick = new Intent();
        onClick.setClassName(Constants.APTOIDE_PACKAGE_NAME, Constants.APTOIDE_PACKAGE_NAME+".DownloadManager");
        onClick.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        onClick.setAction(Constants.APTOIDE_PACKAGE_NAME+".FROM_NOTIFICATION");

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent onClickAction = PendingIntent.getActivity(this, 0, onClick, 0);
        mBuilder.setOngoing(true);
        mBuilder.setContentTitle(getString(R.string.aptoide_downloading, ApplicationAptoide.MARKETNAME))
                .setContentText(getString(R.string.x_app, ongoingDownloads.size()))
                .setSmallIcon(android.R.drawable.stat_sys_download);
        mBuilder.setContentIntent(onClickAction);
        int percentage = getOngoingDownloadsPercentage();
        mBuilder.setProgress(100, percentage, percentage == 0);
        // Displays the progress bar for the first time
        managerNotification.notify(this.hashCode(), mBuilder.build());

    }


    public ArrayList<DownloadInfo> getOngoingDownloads(){
        ArrayList<DownloadInfo> list = new ArrayList<DownloadInfo>();

        for(DownloadInfo downloadInfo: getDownloads()){
            if(!downloadInfo.getStatusState().getEnumState().equals(EnumState.COMPLETE) && !downloadInfo.getStatusState().getEnumState().equals(EnumState.NOSTATE) ){
                list.add(downloadInfo);
            }
        }

        return list;
    }


    public int getOngoingDownloadsPercentage(){

        Collection<DownloadInfo> list = getOngoingDownloads();

        int progressPercentage = 0;
        for(DownloadInfo info : list){
            progressPercentage = progressPercentage + info.getPercentDownloaded();
        }

        if(!list.isEmpty()){
            return progressPercentage / list.size();
        }

        return 0;


    }


}