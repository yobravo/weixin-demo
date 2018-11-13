package com.yobravo;

import org.eclipse.jetty.util.StringUtil;

public class WxSecret {
    private String appId;
    private String appSecret;
    private String serverToken;

    public WxSecret() {
        this.appId = System.getProperty("appId");
        this.appSecret = System.getProperty("appSecret");
        this.serverToken = System.getProperty("serverToken");
        if (StringUtil.isBlank(appId) || StringUtil.isBlank(appSecret) || StringUtil.isBlank(serverToken)) {
            throw new RuntimeException("(appId,appSecret,serverToken)secret credential data " +
                    "are required to integrate weChat server");
        }
    }

    public String getAppID() {
        return appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public String getServerToken() {
        return serverToken;
    }
}
