package org.qii.weiciyuan.support.file;

import android.text.TextUtils;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
 * User: qii
 * Date: 12-8-21
 */
public class FileUploaderHttpHelper {
    public static boolean upload(HttpClient httpClient, String url, Map<String, String> param, String path) {
        AppLogger.d(url);
        HttpPost httpPost = new HttpPost(url);

        MultipartEntity mpEntity = new MultipartEntity();
        FileBody filebody = new FileBody(new File(path));
        mpEntity.addPart("pic", filebody);

        Set<String> keys = param.keySet();
        for (String key : keys) {
            String value = param.get(key);
            if (!TextUtils.isEmpty(value)) {
                try {
                    mpEntity.addPart(key, new StringBody(value, Charset.forName("utf-8")));
                } catch (UnsupportedEncodingException e) {
                    AppLogger.e(e.getMessage());
                    return false;
                }

            }

        }

        httpPost.setEntity(mpEntity);

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            AppLogger.e(e.getMessage());
        }
        String content = null;
        try {
            if (response != null) {
                content = EntityUtils.toString(response.getEntity());
                return true;
            }
        } catch (IOException e) {
            AppLogger.e(e.getMessage());
            return false;
        }
        return false;
    }
}
