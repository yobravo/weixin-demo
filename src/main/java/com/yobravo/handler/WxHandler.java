package com.yobravo.handler;

import com.yobravo.wx.WxPushProcessor;
import com.yobravo.wx.WxPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weixin.popular.util.SignatureUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

@Path("/wx")
public class WxHandler {
    private static final Logger logger = LoggerFactory.getLogger(WxHandler.class);
    private final String serverToken;
    private final WxPushService wxPushService;

    public WxHandler(WxPushService wxPushService, String serverToken) {
        this.wxPushService = wxPushService;
        this.serverToken = serverToken;
    }

    @GET
    @Path("/")
    public Response ackWeChat(@Context Request request,
                              @Context HttpServletRequest httpServletRequest,
                              @QueryParam("signature") String signature,
                              @QueryParam("timestamp") String timestamp,
                              @QueryParam("nonce") String nonce,
                              @QueryParam("echostr") String echostr) {

        if (!signature.equals(SignatureUtil.generateEventMessageSignature(serverToken, timestamp, nonce))) {
            logger.info("The request signature is invalid");
            return Response.status(Response.Status.BAD_REQUEST).entity("The request signature is invalid").build();
        } else {
            logger.info("Ack to weChat that the request signature is valid");
            return Response.status(Response.Status.OK).entity(echostr).build();
        }
    }

    @POST
    @Path("/")
    public void processWeChatPushMsg(@Context HttpServletRequest httpServletRequest,
                                     @Context HttpServletResponse httpServletResponse,
                                     @QueryParam("signature") String signature,
                                     @QueryParam("timestamp") String timestamp,
                                     @QueryParam("nonce") String nonce,
                                     @QueryParam("echostr") String echostr) {
        String oldThreadName = Thread.currentThread().getName();
        String newThreadName = "processWeChatPushMsg_" + System.currentTimeMillis();
        Thread.currentThread().setName(newThreadName);

        logger.info("receive weChat message push, to validate if this is valid weChat push");
        logger.info("request URI: {}", httpServletRequest.getRequestURI());
        if (signature != null && !signature.equals(SignatureUtil.generateEventMessageSignature(serverToken, timestamp, nonce))) {
            logger.info("The request signature:{} is invalid, probably a hacker instead of WeChat", signature);
            return;
        }

        try (WxPushProcessor wxPushProcessor = new WxPushProcessor(httpServletRequest, httpServletResponse);){
            wxPushService.handleWxPush(wxPushProcessor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setName(oldThreadName);
        }
    }
}
