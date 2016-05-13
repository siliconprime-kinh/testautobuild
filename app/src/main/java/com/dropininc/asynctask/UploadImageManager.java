package com.dropininc.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.dropininc.utils.Logs;
import com.loopj.android.http.MySSLSocketFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.HttpVersion;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.EntityBuilder;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.conn.scheme.PlainSocketFactory;
import cz.msebera.android.httpclient.conn.scheme.Scheme;
import cz.msebera.android.httpclient.conn.scheme.SchemeRegistry;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.impl.conn.tsccm.ThreadSafeClientConnManager;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpProtocolParams;
import cz.msebera.android.httpclient.protocol.HTTP;

public class UploadImageManager {
    private Context mContext;
    private String mUrl;
    private String mPathFile;
    private UploadPhotoCallback mUploadPhotoCallback;

    public interface UploadPhotoCallback{
        void onSuccess();
        void onError();
    }

    public UploadImageManager(Context mContext, String mPathFile, String mUrl, UploadPhotoCallback mUploadPhotoCallback) {
        this.mContext = mContext;
        this.mPathFile = mPathFile;
        this.mUrl = mUrl;
        this.mUploadPhotoCallback = mUploadPhotoCallback;

        new UploadPhotoAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUrl, mPathFile);
    }

    private class UploadPhotoAsync extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Logs.log("UploadPhotoAsync", "onPreExecute");
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);

                SSLSocketFactory sf = (new MySSLSocketFactory(trustStore));
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                BasicHttpParams httpParams = new BasicHttpParams();
                HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(httpParams, HTTP.DEFAULT_CONTENT_CHARSET);
                HttpProtocolParams.setUseExpectContinue(httpParams, true);

                SchemeRegistry sr = new SchemeRegistry();
                sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                sr.register(new Scheme("https", sf, 443));
                ThreadSafeClientConnManager ccManager = new ThreadSafeClientConnManager(httpParams, sr);

                HttpClient httpClient = new DefaultHttpClient(ccManager, httpParams);
                HttpPut httpPut = new HttpPut(params[0]);
                httpPut.addHeader("Content-Type","binary/octet-stream");
                File file = new File(params[1]);
                Logs.log("UploadPhotoAsync", "Image: " + file.getName() + " - length: "
                        + file.length() + " to " + params[0]);
                EntityBuilder entityBuilder = EntityBuilder.create();
                ByteArrayEntity byteArrayEntity =  new ByteArrayEntity(read(file));
                if (file != null) {
                    entityBuilder.setFile(file);
                } else {
                    Logs.log("UploadPhotoAsync", "Image File not exist");
                }
                httpPut.setEntity(byteArrayEntity);
                for (int i = 0; i < httpPut.getAllHeaders().length; i++) {
                    Logs.log("UploadPhotoAsync", "header: " + httpPut.getAllHeaders()[0].getName()
                            + " - value: " + httpPut.getAllHeaders()[0].getValue());
                }
                HttpResponse response = httpClient.execute(httpPut);
                int responseCode = response.getStatusLine().getStatusCode();
                Logs.log("ResponseUploadPhoto", "ResponseCode: " + responseCode);
                return responseCode;
            } catch (Exception e) {
                Logs.log(e);
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == HttpStatus.SC_OK) {
                mUploadPhotoCallback.onSuccess();
            }else{
                mUploadPhotoCallback.onError();
            }
        }
    }

    public String inputStreamToString(InputStream is) {
        StringBuffer s = new StringBuffer();
        String line = "";
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try {
            while ((line = rd.readLine()) != null) {
                s.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logs.log("ResponseUploadPhoto", s.toString());
        return s.toString();
    }

    public byte[] read(File file) throws IOException {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }
}
