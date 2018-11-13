package com.yobravo;

import com.yobravo.handler.WxHandler;
import com.yobravo.utils.SslHttpClient;
import com.yobravo.wx.WxAccessTokenProvider;
import com.yobravo.wx.WxInterfaceController;
import com.yobravo.wx.WxPushService;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;


public class RestServer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RestServer.class);
    private final Server jettyServer;
    private final List<Object> components;
    private final String contextPath;
    private final HttpClient trustAllSslClient;

    public RestServer(int port, String contextPath, WxSecret wxSecret, boolean isHttps, SslContextFactory sslContextFactory) throws Exception{
        makeJavaUtilLoggingRedirectToSlf4j();
        this.contextPath = contextPath;

        jettyServer = new Server();
        ServerConnector connector = isHttps?
                getHttpsServerConnector(jettyServer, port, sslContextFactory) :getHttpServerConnector(jettyServer, port);
        jettyServer.addConnector(connector);
        trustAllSslClient = SslHttpClient.getTrustAllSslClient();

        components = new ArrayList<>();
        components.add(getWxHandler(trustAllSslClient, wxSecret));

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[]{getResourceContext(components)});
        jettyServer.setHandler(handlerList);
    }

    private Object getWxHandler(HttpClient trustAllSslClient, WxSecret wxSecret) throws Exception{
        if(!trustAllSslClient.isStarted()){
            trustAllSslClient.start();
        }

        String appID = wxSecret.getAppID();
        String appSecret = wxSecret.getAppSecret();
        String serverToken = wxSecret.getServerToken();
        WxAccessTokenProvider wxAccessTokenProvider = new WxAccessTokenProvider(trustAllSslClient, appID, appSecret);
        WxInterfaceController wxInterfaceController = new WxInterfaceController(wxAccessTokenProvider, trustAllSslClient);
        WxPushService wxPushService = new WxPushService(wxInterfaceController);
        return new WxHandler(wxPushService, serverToken);
    }

    private ServerConnector getHttpServerConnector(Server server, int httpPort) {
        HttpConfiguration config = new HttpConfiguration();
        config.setRequestHeaderSize(8 * 8192);
        config.setSendServerVersion(false);
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(config));
        connector.setIdleTimeout(10000);
        connector.setPort(httpPort);
        return connector;
    }

    private ServerConnector getHttpsServerConnector(Server server, int httpsPort, SslContextFactory sslContextFactory){
        HttpConfiguration config = new HttpConfiguration();
        ServerConnector connector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "HTTP/1.1"), new HttpConnectionFactory(config));
        connector.setPort(httpsPort);
        config.setSendServerVersion(false);
        config.setRequestHeaderSize(8 * 8192);
        connector.setIdleTimeout(10000);
        return connector;
    }

    private static void makeJavaUtilLoggingRedirectToSlf4j() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    public void start() throws Exception {
        if(!trustAllSslClient.isStarted()){
            trustAllSslClient.start();
        }
        jettyServer.start();
    }

    public void stop() {
        try {
            trustAllSslClient.stop();
            jettyServer.stop();
        } catch (Exception e) {
            log.info("Fail to stop jetty server.");
        }
        for (Object component : components) {
            if (component instanceof Closeable) {
                try {
                    ((Closeable) component).close();
                } catch (IOException e) {
                    log.info("Error while closing " + component, e);
                }
            }
        }
    }

    private Handler getResourceContext(List<Object> components) {
        ServletContextHandler resourceContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        resourceContext.setContextPath(contextPath);

        ResourceConfig resourceConfig = new ResourceConfig();
        components.forEach(resourceConfig::register);
        resourceContext.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");
        return resourceContext;
    }
}
