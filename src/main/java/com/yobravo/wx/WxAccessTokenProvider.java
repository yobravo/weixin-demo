package com.yobravo.wx;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.StringUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.yobravo.wx.WxApiUrl.*;
import static com.yobravo.wx.WxConstant.TOKEN_RENEW_INTERVAL_MIN;

public class WxAccessTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(WxAccessTokenProvider.class);
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private HttpClient httpClient;
    private volatile String accessToken;
    private final String accessTokenApi;

    public WxAccessTokenProvider(HttpClient httpClient, String appId, String appSecret) {
        this.httpClient = httpClient;
        this.accessTokenApi = $ACCESS_TOKEN_API_GET.replace($APPID, appId).replace($APPSECRET, appSecret);
        this.accessToken = requestNewAccessToken();
        this.autoRefreshAccessToken();
    }

    private void autoRefreshAccessToken() {
        executorService.scheduleAtFixedRate(()->{
            try {
                this.requestNewAccessToken();
            }catch (Exception e){
                e.printStackTrace();
            }
        }, TOKEN_RENEW_INTERVAL_MIN, TOKEN_RENEW_INTERVAL_MIN, TimeUnit.MINUTES);
    }


    private String requestNewAccessToken() {
        for (int i = 0; i < 5; i++) {
            try {
                logger.info("to renew weChat access token, which will expire after 2 hours");
                ContentResponse response = httpClient.newRequest(accessTokenApi)
                        .method(HttpMethod.GET)
                        .send();
                if (response.getStatus() == 200) {
                    JSONObject jsonObject = new JSONObject(response.getContentAsString());
                    if (40164 == jsonObject.optInt("errcode")) {
                        logger.error("Invalid IP address, need to put into white list in weChat backend management page, resp:" + response.getContentAsString());
                        throw new RuntimeException("Invalid IP address, need to put into white list in weChat backend management page");
                    } else {
                        if (StringUtil.isNotBlank(jsonObject.optString("access_token"))) {
                            String accessToken = jsonObject.getString("access_token");
                            logger.info("Renew access success success: => {}", accessToken);
                            return this.accessToken = accessToken;
                        } else {
                            logger.error("cannot renew access token: {}", response.getContentAsString());
                            Thread.sleep(10 * 1000);
                        }
                    }
                } else {
                    logger.info("access token refresh failed code:{}, body:{}, to retry ", response.getStatus(), response.getContentAsString());
                    Thread.sleep(10 * 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("error request for wx access token", e);
            }
        }
        //fail to get token;
        throw new RuntimeException("Failed to get access token after 5 time");
    }


    public synchronized String getAccessToken() {
        return this.accessToken;
    }
}
