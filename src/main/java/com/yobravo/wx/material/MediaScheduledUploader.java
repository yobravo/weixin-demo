package com.yobravo.wx.material;

import com.yobravo.wx.WxAccessTokenProvider;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weixin.popular.api.MediaAPI;
import weixin.popular.bean.media.Media;
import weixin.popular.bean.media.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MediaScheduledUploader {
    private static final Logger logger = LoggerFactory.getLogger(MediaScheduledUploader.class);
    private volatile List<String> uploadedMediaIDs = new ArrayList<>();
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final String CP_MEDIA_FILES_FOLDER = "media_image";
    private static final Pattern imagePattern = Pattern.compile(".*\\.jpg");

    public MediaScheduledUploader(WxAccessTokenProvider wxAccessTokenProvider) {
        this.scheduleUploading(wxAccessTokenProvider);
    }

    public synchronized List<String> getUploadedMediaIDs() {
        return this.uploadedMediaIDs;
    }

    private void scheduleUploading(final WxAccessTokenProvider wxAccessTokenProvider) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Thread.sleep(10 * 1000);
                // 未认证公众号不是使用素材接口管理素材， 仅仅可以上传临时素材，不能永久，有效三天
                logger.info("upload the material image");
                String accessToken = getOrWaitAccessToken(wxAccessTokenProvider);
                this.uploadedMediaIDs = uploadImageGetMediaIds(accessToken);
                logger.info("we got media_id list: {}", uploadedMediaIDs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 60, TimeUnit.HOURS);
    }

    private List<String> uploadImageGetMediaIds(String accessToken) throws Exception {
        List<String> mediaFileNameToUpload = getMediaFileNameToUpload(CP_MEDIA_FILES_FOLDER);
        return upload(accessToken, mediaFileNameToUpload);
    }

    private List<String> upload(String accessToken, List<String> cpFileNames) throws Exception {
        List<String> mediaIdList = new ArrayList<>();
        ClassLoader classLoader = MediaScheduledUploader.class.getClassLoader();
        for (String fileName : cpFileNames) {
            try {
                String cpFile = fileName;
                logger.info("to upload file:{}", cpFile);
                try (InputStream is = classLoader.getResourceAsStream(cpFile)) {
                    Media media = MediaAPI.mediaUpload(accessToken, MediaType.image, is);
                    if (media.isSuccess()) {
                        logger.info("Successfully upload file: {}, get mediaId ={}", cpFile, media.getMedia_id());
                        mediaIdList.add(media.getMedia_id());
                    } else {
                        logger.error("Failed upload file: {}", cpFile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mediaIdList;
    }


    private List<String> getMediaFileNameToUpload(String mediaFolderPath) throws Exception {
        Reflections reflections = new Reflections(mediaFolderPath, new ResourcesScanner());
        Set<String> resources = reflections.getResources(imagePattern);
        logger.info("---" + resources);
        return new ArrayList<>(resources);
    }

    private String getOrWaitAccessToken(WxAccessTokenProvider accessTokenProvider) throws Exception {
        for (int i = 0; i < 5; i++) {
            String accessToken = accessTokenProvider.getAccessToken();
            if (accessToken != null) {
                logger.info("access token acquired");
                return accessToken;
            } else {
                logger.info("wait WxAccessTokenProvider to request for access token");
                Thread.sleep(10 * 1000);
            }
        }
        throw new RuntimeException("Failed to get access token");
    }

    private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();
        try (InputStream in = getResourceAsStream(path);
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;
            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }
        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

}
