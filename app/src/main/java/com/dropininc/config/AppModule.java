package com.dropininc.config;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import com.dropininc.AppApplication;
import com.dropininc.BuildConfig;
import com.dropininc.network.IGoogleMapsApi;
import com.dropininc.network.IServiceApi;
import com.dropininc.sharepreference.DSharePreference;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.inject.Singleton;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class AppModule {

    @Singleton
    @Provides
    public Context provideContext() {
        return AppApplication.getInstance();
    }

    @Singleton
    @Provides
    public IServiceApi provideServiceApi(Retrofit retrofit) {
        return retrofit.create(IServiceApi.class);
    }

    @Singleton
    @Provides
    public Retrofit provideRetrofit(Context context) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        Interceptor headerInterceptor = chain -> {
            Request original = chain.request();

            Request.Builder builder = original.newBuilder()
                    .header("apiVersion", "1.1.4")
                    .header("Content-Type", "application/json; charset=UTF-8");

            if (!TextUtils.isEmpty(DSharePreference.getAccessToken(context))) {
                builder.header("Authorization", "Bearer " + DSharePreference.getAccessToken(context));
            }

            String url = original.url().toString();
            if (BuildConfig.DEBUG) {
                if (url.contains("?"))
                    url += "&debug=true";
                else
                    url += "?debug=true";
            }

            Request request = builder.method(original.method(), original.body()).url(url).build();

            try {
                return chain.proceed(request);
            } catch (SSLHandshakeException e) {
                e.printStackTrace();
            }

            return new Response.Builder()
                    .code(500)
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body(ResponseBody.create(
                            MediaType.parse("application/json; charset=utf-8"), new byte[]{}))
                    .build();
        };

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(interceptor)
                .addInterceptor(headerInterceptor);

        /*disableSSLCertificate*/
        /*
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(final String hostname,
                                  final SSLSession session) {
                return true;
            }
        };

        SSLSocketFactory tt = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            tt =  sc.getSocketFactory();
        } catch (KeyManagementException e) {
            tt = null;
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            tt = null;
            e.printStackTrace();
        }
        if(tt != null){

            try {

                builder.hostnameVerifier(hostnameVerifier);
                builder.sslSocketFactory(tt);
                Log.d("KINH", "HTTPS>>>disableSSLCertificate OKOK");
            } catch (final Exception e) {
                Log.d("KINH", "HTTPS>>>disableSSLCertificate FAIL.Exception=" + e!= null ? e.getMessage():"");
            }
        }else{
            Log.d("KINH", "HTTPS>>>disableSSLCertificate FAIL.SSLSocketFactory");
        }
        */
        /**/

        OkHttpClient client = builder.build();



        String url = DSharePreference.getDebugURL(context);
        //Log.d("API", "get API: " + url);

        return new Retrofit.Builder()
//                .baseUrl(Constants.SERVER_URL)
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();
    }

    @Singleton
    @Provides
    public IGoogleMapsApi provideGoogleMapsApi(Context context) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(interceptor);

        OkHttpClient client = builder.build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        return retrofit.create(IGoogleMapsApi.class);
    }

}
