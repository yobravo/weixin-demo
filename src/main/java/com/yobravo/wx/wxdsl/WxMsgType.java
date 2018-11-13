package com.yobravo.wx.wxdsl;

public enum WxMsgType {
    text("text"),
    image("image"),
    voice("voice"),
    video("video"),
    shortvideo("shortvideo"),
    location("location"),
    link("link"),
    event("event");

    private final String msgType;

    WxMsgType(String msgType){
        this.msgType = msgType;
    }

    public String getMsgType() {
        return msgType;
    }

    public static boolean isMsgTypeValid(String msgType){
        for (WxMsgType type : WxMsgType.values()) {
            if(type.getMsgType().equalsIgnoreCase(msgType)){
                return true;
            }
        }
        return false;
    }
}
