package com.yobravo.wx.material;

import com.yobravo.wx.WxAccessTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weixin.popular.api.MaterialAPI;
import weixin.popular.bean.material.MaterialBatchgetResult;
import weixin.popular.bean.material.MaterialBatchgetResultItem;

import java.util.List;
import java.util.Random;

public class ImageResourceProvider {
    private static final Logger logger = LoggerFactory.getLogger(ImageResourceProvider.class);
    private final WxAccessTokenProvider accessTokenProvider;
    private static final String IMAGE = "image";
    private final Random random = new Random();
    private final MediaScheduledUploader mediaScheduledUploader;

    public ImageResourceProvider(WxAccessTokenProvider accessTokenProvider) {
        this.accessTokenProvider = accessTokenProvider;
        this.mediaScheduledUploader = new MediaScheduledUploader(accessTokenProvider);
    }

    //this is just for testing,it schedule upload temporary files(valid for 3 days only) and for responding  to users
    public String getRandomImageMediaIdHardcoded() {
        List<String> mediaIds = mediaScheduledUploader.getUploadedMediaIDs();
        if (mediaIds.size() > 0) {
            return mediaIds.get(random.nextInt(mediaIds.size()));
        }
        logger.error("there is no uploaded medias at all");
        return "";
    }

    // 未认证公众号是不能访问素材管理的 ：https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1433401084
    public String getRandomImageMediaId() throws Exception {
        String accessToken = this.accessTokenProvider.getAccessToken();
        Integer image_count = MaterialAPI.get_materialcount(accessToken).getImage_count();
        for (int i = 0; i < 5; i++) {
            try {
                int offset = random.nextInt(image_count - 1);
                MaterialBatchgetResult materialBatchgetResult = MaterialAPI.batchget_material(accessToken, IMAGE, offset, 1);
                if (materialBatchgetResult.isSuccess()) {
                    MaterialBatchgetResultItem materialBatchgetResultItem = materialBatchgetResult.getItem().get(0);
                    return materialBatchgetResultItem.getMedia_id();
                } else {
                    logger.warn("Fail to get a random IMAGE mediaId, retry");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Failed to get any IMAGE media");
    }
}
