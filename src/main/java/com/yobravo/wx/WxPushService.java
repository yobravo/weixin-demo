package com.yobravo.wx;

import com.yobravo.wx.testresource.MotivationalQuotes;
import com.yobravo.wx.wxdsl.WxEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;


import static com.yobravo.wx.wxdsl.WxEventType.subscribe;
import static com.yobravo.wx.wxdsl.WxMsgType.isMsgTypeValid;

public class WxPushService {
    private static final Logger logger = LoggerFactory.getLogger(WxPushService.class);
    private final WxInterfaceController wxInterfaceController;
    private Random random = new Random();

    public WxPushService(WxInterfaceController wxInterfaceController) {
        this.wxInterfaceController = wxInterfaceController;
    }

    public void handleWxPush(WxPushProcessor wxPushProcessor) throws Exception {
        Map<String, String> wxPushMsg = wxPushProcessor.getWxPushMsg();
        String msgType = wxPushMsg.get("MsgType");
        logger.info("receive weChat message type: {}, request body: {}", msgType, wxPushMsg.toString());
        if (msgType == null || !isMsgTypeValid(msgType)) {
            logger.error("not found or un-recognize/un-config weChat message type, get webChat msg: {}", wxPushMsg.toString());
            wxPushProcessor.rejectInvalidPush();
            return;
        }

        switch (wxPushProcessor.getWxMsgType()) {
            case event: {
                logger.info("get weChat push event");
                if (subscribe == WxEventType.valueOf(wxPushMsg.get("Event"))) {
                    logger.info("it is a new user subscribe event, to say welcome");
                    String thanks = MotivationalQuotes.getRandomQuote();
                    wxPushProcessor.respWithXmlText(thanks);
                } else {
                    wxPushProcessor.ignoreWxPushMsg();
                }
                return;
            }

            case text: {
                logger.info("weChat push a fan's text message");
                if (random.nextBoolean()) {
                    wxPushProcessor.respWithXmlText(MotivationalQuotes.getRandomQuote());
                } else {
                    wxPushProcessor.respWithImage(wxInterfaceController.getRandomImageMedia());
                }
                return;
            }

            case image: {
                logger.info("weChat push a fan's IMAGE message");
                wxPushProcessor.respWithImage(wxInterfaceController.getRandomImageMedia());
                return;
            }

            default: {
                //TODO more business logic to be implemented
                logger.info("weChat push: {}, handling logic to be implemented", wxPushMsg.toString());
                wxPushProcessor.respWithXmlText(MotivationalQuotes.getRandomQuote());
                return;
            }
        }
    }
}
