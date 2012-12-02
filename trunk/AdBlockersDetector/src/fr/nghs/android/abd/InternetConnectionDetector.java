package fr.nghs.android.abd;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;

public class InternetConnectionDetector
{

    /**
    *  Asynchronous callback
    */
    public static interface Callback
    {
        /**
        * Called in the GUI thread when result is available.
        * @param isConnected true if connected to the Internet.
        */
        public void onResult(boolean isConnected);
    }

    /**
    * Asynchronous Internet connection detection.
    * Callback is called in GUI thread.
    * @param callback
    */
    public void detectConnection(Callback callback)
    {
        new DetectTask(callback).execute();
    }

    /**
    * Synchronous Internet connection detection
    * @return true if the Internet is reachable
    */
    public boolean detectConnection()
    {
        for(final String h : HTTP_HOSTS)
            if(isReachable(h))
                return true;
        return false;
    }

    public static final boolean isReachable(String httpHost)
    {
        try
        {
            final HttpURLConnection c = (HttpURLConnection)new URL("http", httpHost, "/").openConnection();
            c.setConnectTimeout(TIMEOUT_MS);
            c.setReadTimeout(TIMEOUT_MS);
            c.setRequestMethod("HEAD");
            final int responseCode = c.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        }
        catch(IOException exception)
        {
            return false;
        }	
    }

    private static final int TIMEOUT_MS = 6000;

    private static final String [] HTTP_HOSTS =
        {
        "www.google.com",
        "www.yahoo.com",
        "www.baidu.com",
        "www.msn.com",
        "www.amazon.com",
        "www.ebay.com",
        "www.wikipedia.org"
        };

    private class DetectTask extends AsyncTask<Void, Void, Boolean>
    {

        private WeakReference<Callback> callback;

        public DetectTask(Callback c)
        {
            callback = new WeakReference<Callback>(c);
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                return detectConnection();
            }
            catch(Throwable t)
            {
                return false;		        
            }
        }

        @Override
        protected void onPostExecute(Boolean r)
        {
            final Callback c = callback.get();
            if(c != null && r != null)
                c.onResult(r);
        }

    }
}
