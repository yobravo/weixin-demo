package com.yobravo.wx.wxdsl;

public enum  WxEventType {
    subscribe("subscribe"),
    unsubscribe("unsubscribe"),
    SCAN("SCAN"),
    LOCATION("LOCATION"),
    CLICK("CLICK"),
    VIEW("VIEW");

    private final String eventType;

    WxEventType(String eventType){
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public static boolean isEventTypeValid(String eventType){
        for (WxEventType type : WxEventType.values()) {
            if(type.getEventType().equalsIgnoreCase(eventType)){
                return true;
            }
        }
        return false;
    }
}
