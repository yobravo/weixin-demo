package com.yobravo.wx;

import com.yobravo.wx.wxdsl.WxMsgType;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weixin.popular.bean.xmlmessage.XMLImageMessage;
import weixin.popular.bean.xmlmessage.XMLMessage;
import weixin.popular.bean.xmlmessage.XMLTextMessage;
import weixin.popular.support.ExpireKey;
import weixin.popular.support.expirekey.DefaultExpireKey;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WxPushProcessor implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(WxPushProcessor.class);
    private static ExpireKey expireKeyTracker = new DefaultExpireKey();

    private final Map<String, String> wxMsg;
    private final ServletOutputStream outputStream;

    public WxPushProcessor(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        this.wxMsg = parseXml(httpServletRequest);
        this.outputStream = httpServletResponse.getOutputStream();
    }

    public WxMsgType getWxMsgType() {
        String msgType = wxMsg.get("MsgType");
        return WxMsgType.valueOf(msgType);
    }

    public Map<String, String> getWxPushMsg() {
        return this.wxMsg;
    }

    public void ignoreWxPushMsg() {
        output("");
    }

    public void rejectInvalidPush() throws Exception {
        String text = "un-recognized weChat message, please try again";
        outputStream.write(text.getBytes("utf-8"));
        outputStream.flush();
    }

    public void respWithImage(String mediaId) throws Exception {
        if (isValidWxPush()) {
            XMLImageMessage xmlImageMessage = new XMLImageMessage(wxMsg.get("FromUserName"), wxMsg.get("ToUserName"), mediaId);
            xmlImageMessage.outputStreamWrite(outputStream);
        }
    }

    public void respWithXmlText(String textContent) throws Exception {
        if (isValidWxPush()) {
            XMLMessage xmlTextMessage = new XMLTextMessage(wxMsg.get("FromUserName"), wxMsg.get("ToUserName"), textContent);
            xmlTextMessage.outputStreamWrite(outputStream);
        }
    }

    private void output(String textContent) {
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(outputStream));
            pw.write(textContent);
            logger.info("**************** return value ***************=" + textContent);
            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean outputStreamWriteText(String textContent) throws Exception {
        try {
            outputStream.write(textContent.getBytes("utf-8"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidWxPush() {
        String key = wxMsgKey(wxMsg);
        logger.info("response to message key：" + key);

        if (expireKeyTracker.exists(key)) {
            logger.info("it is duplicate weChat push, to tell weChat to stop sending the same message");
            ignoreWxPushMsg();
            return false;
        } else {
            logger.info("valid weChat push, to track it");
            expireKeyTracker.add(key);
            return true;
        }
    }

    private String wxMsgKey(Map<String, String> wxMsg) {
        return wxMsg.get("FromUserName") + "_"
                + wxMsg.get("ToUserName") + "_"
                + wxMsg.get("MsgId") + "_"
                + wxMsg.get("CreateTime");
    }


    private Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        Map<String, String> map = new HashMap<>();
        InputStream inputStream = request.getInputStream();

        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();
        List<Element> elementList = root.elements();

        for (Element e : elementList)
            map.put(e.getName(), e.getText());

        inputStream.close();
        return map;
    }

    @Override
    public void close() throws Exception {
        if (outputStream != null) {
            outputStream.close();
        }
    }
}
