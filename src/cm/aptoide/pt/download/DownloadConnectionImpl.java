package cm.aptoide.pt.download;

import android.util.Log;
import cm.aptoide.pt.ApplicationAptoide;
import cm.aptoide.pt.util.NetworkUtils;
import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-07-2013
 * Time: 15:44
 * To change this template use File | Settings | File Templates.
 */
public class DownloadConnectionImpl extends DownloadConnection {

    HttpURLConnection connection;
    private BufferedInputStream mStream;
    private final static int TIME_OUT = 10000;


    public DownloadConnectionImpl(URL url) throws IOException {
        super(url);
    }

    @Override
    public long connect(long downloaded) throws IOException, CompletedDownloadException, NotFoundException, IPBlackListedException {
        connection = (HttpURLConnection) this.mURL.openConnection();


        connection.setConnectTimeout(TIME_OUT);
        connection.setReadTimeout(TIME_OUT);
        connection.setRequestProperty("User-Agent", NetworkUtils.getUserAgentString(ApplicationAptoide.getContext()));

        if (downloaded > 0L) {
            // server must support partial content for resume
            connection.addRequestProperty("Range", "bytes=" + downloaded + "-");
            int responseCode = connection.getResponseCode();
            Log.d("DownloadManager", "Response Code is: " + responseCode);
            if(responseCode == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE){
                throw new CompletedDownloadException(getSize());
            }else if (responseCode != HttpStatus.SC_PARTIAL_CONTENT) {
                throw new IOException("Server doesn't support partial content.");
            }
        } else if (connection.getResponseCode() != HttpStatus.SC_OK) {
            int responseCode = connection.getResponseCode();
            if(responseCode == 404){
                throw new NotFoundException();
            }else if(responseCode == 403){
                throw new IPBlackListedException();
            }
            // response not ok
            throw new IOException("Cannot retrieve file from server.");
        }

        mStream = new BufferedInputStream(connection.getInputStream(), 8 * 1024);

        return getSize();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getSize() throws IOException {
        return connection.getContentLength();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() {
        connection.disconnect();
    }

    @Override
    public BufferedInputStream getStream() {
        return mStream;
    }

    @Override
    public long getShallowSize() throws IOException {
        return mURL.openConnection().getContentLength();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
