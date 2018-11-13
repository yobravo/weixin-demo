package com.yobravo.wx;

public class WxApiUrl {
    public static final String $APPID = "${APPID}";
    public static final String $APPSECRET = "${APPSECRET}";
    public static final String $OPENID = "${OPENID}";
    public static final String $ACCESS_TOKEN = "${ACCESS_TOKEN}";

    public static final String $ACCESS_TOKEN_API_GET = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${APPID}&secret=${APPSECRET}";
    public static final String $USER_OPENID_API_GET = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=${ACCESS_TOKEN}&openid=${OPENID}&lang=zh_CN";


}
