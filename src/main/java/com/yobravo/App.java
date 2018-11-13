package com.yobravo;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.KeyStore;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static RestServer server;
    private static final String contextPath = "/";
    private static final String defaultPort = "443"; // WeChat only accept 80 or 443, but need to use root user

    public static void main(String[] args) {
        try {
            WxSecret wxSecret = new WxSecret();
            String port = System.getProperty("port", defaultPort);
            boolean isHttps = "443".equals(port);
            log.info("use port: {}, is ssl: {} ", port, isHttps);

            SslContextFactory sslContextFactory = createTestSslContextFactory();
            server = new RestServer(Integer.parseInt(port), contextPath, wxSecret, isHttps, sslContextFactory);
            start();
            log.info("App started successfully");
            Runtime.getRuntime().addShutdownHook(new Thread(App::stop));
        } catch (Throwable t) {
            log.error("Error during startup", t);
            System.exit(1);
        }
    }

    private static void start() {
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void stop() {
        server.stop();
    }

    private static SslContextFactory createTestSslContextFactory() throws Exception{
        try(InputStream inputStream = App.class.getClassLoader().getResourceAsStream("keystore.jks")){
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setTrustStoreType("JCEKS");
            sslContextFactory.setKeyStoreType("JCEKS");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(inputStream, "changeit".toCharArray());
            sslContextFactory.setKeyStore(keyStore);
            sslContextFactory.setKeyManagerPassword("changeit");
            sslContextFactory.addExcludeProtocols("SSLv3", "SSLv2", "SSLv2Hello");
            sslContextFactory.setExcludeCipherSuites(".*MD5$", ".*RSA.*128.*SHA$");
            return sslContextFactory;
        }
    }
}
