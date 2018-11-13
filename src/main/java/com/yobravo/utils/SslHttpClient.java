package com.yobravo.utils;


import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SslHttpClient {
    public static HttpClient getTrustAllSslClient() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getX509TrustManagers(), new SecureRandom());
            sslContextFactory.setSslContext(sslContext);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((var0, var1) -> true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new HttpClient(sslContextFactory);
    }

    public static HttpClient getSslHttpClient(String keystoreClasspath, String password) {
        SslContextFactory sslContextFactory = new SslContextFactory();
        try {
            InputStream ki = SslHttpClient.class.getClassLoader().getResourceAsStream(keystoreClasspath);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(ki, password.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SUNX509");
            kmf.init(keyStore, password.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("SSL");
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((var0, var1) -> true);
        } catch (Exception t) {
            t.printStackTrace();
        }
        return new HttpClient(sslContextFactory);
    }


    private static TrustManager[] getX509TrustManagers() {
        X509TrustManager x509TrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        return new X509TrustManager[]{x509TrustManager};
    }
}
