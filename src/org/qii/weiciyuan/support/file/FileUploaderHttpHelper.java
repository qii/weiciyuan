package org.qii.weiciyuan.support.file;

import android.text.TextUtils;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
 * User: qii
 * Date: 12-8-21
 */
public class FileUploaderHttpHelper {
    public static boolean upload(HttpClient httpClient, String url, Map<String, String> param, String path, final ProgressListener listener) {
        AppLogger.d(url);
        HttpPost httpPost = new HttpPost(url);

        MultipartEntity mpEntity = new MultipartEntity() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                super.writeTo(new CountingOutputStream(outstream, listener));
            }
        };
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
        try {
            if (response != null) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    EntityUtils.consume(response.getEntity());
                    return true;
                } else {
                    EntityUtils.consume(response.getEntity());
                }
            }
        } catch (IOException e) {
            AppLogger.e(e.getMessage());
            return false;
        }
        return false;
    }

    public static interface ProgressListener {
        public void transferred(long data);
    }

    public static class CountingOutputStream extends FilterOutputStream {

        private final ProgressListener listener;
        private long transferred;

        public CountingOutputStream(final OutputStream out,
                                    final ProgressListener listener) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }


        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred);
        }

        public void write(int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred);
        }
    }
}
