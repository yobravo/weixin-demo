package com.yobravo.wx;

import com.yobravo.wx.material.ImageResourceProvider;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.yobravo.wx.WxApiUrl.*;


public class WxInterfaceController {
    private static final Logger logger = LoggerFactory.getLogger(WxInterfaceController.class);
    private final WxAccessTokenProvider wxAccessTokenProvider;
    private final HttpClient httpClient;
    private final ImageResourceProvider imageResourceProvider;

    public WxInterfaceController(WxAccessTokenProvider wxAccessTokenProvider, HttpClient httpClient) {
        this.wxAccessTokenProvider = wxAccessTokenProvider;
        this.httpClient = httpClient;
        this.imageResourceProvider = new ImageResourceProvider(wxAccessTokenProvider);
    }

    public String getRandomImageMedia() throws Exception {
        return this.imageResourceProvider.getRandomImageMediaIdHardcoded();
    }

    // 未认证不能访问：https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1433401084
    public String getUserNickname(String userOpenId) {
        String nickName = null;
        logger.info("call weChat to get openId={} mapping nickname", userOpenId);
        String api = $USER_OPENID_API_GET.replace($OPENID, userOpenId).replace($ACCESS_TOKEN, wxAccessTokenProvider.getAccessToken());
        try {
            ContentResponse response = httpClient.newRequest(api)
                    .method(HttpMethod.GET)
                    .timeout(10, TimeUnit.SECONDS)
                    .send();
            if (response.getStatus() == 200) {
                String respStr = response.getContentAsString();
                logger.info("weChat returns openId details: {} ", respStr);
                nickName = new JSONObject(respStr).optString("nickname");
            } else {
                logger.info("Failed to get user nick name by user openId: {}", userOpenId);
                logger.info("response status: {}, response body: {}", response.getStatus(), response.getContentAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nickName != null ? nickName : "";
    }
}
