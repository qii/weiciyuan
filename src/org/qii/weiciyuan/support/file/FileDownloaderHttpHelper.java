package org.qii.weiciyuan.support.file;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.StatusLine;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.io.*;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 * Time: 上午9:44
 */
public class FileDownloaderHttpHelper {

    public static interface DownloadListener {
        public void pushProgress(int progress, int max);
    }


    public static String saveFile(HttpResponse response, String path, FileDownloaderHttpHelper.DownloadListener downloadListener) {

        StatusLine status = response.getStatusLine();
        int statusCode = status.getStatusCode();


        if (statusCode != HttpStatus.SC_OK) {
            return dealWithError(response);
        }


        return saveFileAndGetFileAbsolutePath(response, path, downloadListener);

    }

    private static String dealWithError(HttpResponse response) {

        return "";
    }

    private static String saveFileAndGetFileAbsolutePath(HttpResponse response, String path, FileDownloaderHttpHelper.DownloadListener downloadListener) {
        HttpEntity httpEntity = response.getEntity();
//        FileManager.createNoMediaFile();
        File file = FileManager.createNewFileInSDCard(path);
        FileOutputStream out = null;
        InputStream in = null;
        String result = "";

        if (file != null) {
            try {
                int bytetotal = (int) httpEntity.getContentLength();
                int bytesum = 0;
                int byteread = 0;
                out = new FileOutputStream(file);
                in = httpEntity.getContent();
                byte[] buffer = new byte[1444];
                while ((byteread = in.read(buffer)) != -1) {
                    bytesum += byteread;
                    out.write(buffer, 0, byteread);
                    if (downloadListener != null && bytetotal > 0) {
                        downloadListener.pushProgress(bytesum, bytetotal);
                    }
                }
                result = path;
            } catch (FileNotFoundException ignored) {
            } catch (IOException e) {

            } finally {

                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {

                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {

                    }
                }

            }

        }

        try {
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            AppLogger.e(e.getMessage());
        }

        return result;
    }


}
