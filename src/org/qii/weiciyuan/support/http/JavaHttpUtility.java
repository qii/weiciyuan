package org.qii.weiciyuan.support.http;

import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.file.FileUploaderHttpHelper;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * User: qii
 * Date: 12-12-19
 */
public class JavaHttpUtility {
    public String executeNormalTask(HttpMethod httpMethod, String url, Map<String, String> param) throws WeiboException {
        switch (httpMethod) {
            case Post:
                return doPost(url, param);
            case Get:
                return doGet(url, param);
        }
        return "";
    }

    public String doPost(String urlAddress, Map<String, String> param) throws WeiboException {
        GlobalContext globalContext = GlobalContext.getInstance();
        String errorStr = globalContext.getString(R.string.timeout);
        globalContext = null;
        try {
            URL url = new URL(urlAddress);
            HttpURLConnection uRLConnection = (HttpURLConnection) url.openConnection();
            uRLConnection.setDoInput(true);
            uRLConnection.setDoOutput(true);
            uRLConnection.setRequestMethod("POST");
            uRLConnection.setUseCaches(false);
            uRLConnection.setConnectTimeout(5000);
            uRLConnection.setReadTimeout(8000);
            uRLConnection.setInstanceFollowRedirects(false);
            uRLConnection.setRequestProperty("Connection", "Keep-Alive");
            uRLConnection.setRequestProperty("Charset", "UTF-8");
            uRLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            uRLConnection.connect();

            DataOutputStream out = new DataOutputStream(uRLConnection.getOutputStream());
            out.write(Utility.encodeUrl(param).getBytes());
            out.flush();
            out.close();
            return handleResponse(uRLConnection);
        } catch (IOException e) {
            e.printStackTrace();
            throw new WeiboException(errorStr, e);
        }
    }

    private String handleResponse(HttpURLConnection httpURLConnection) throws WeiboException {
        GlobalContext globalContext = GlobalContext.getInstance();
        String errorStr = globalContext.getString(R.string.timeout);
        globalContext = null;
        int status = 0;
        try {
            status = httpURLConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            httpURLConnection.disconnect();
            throw new WeiboException(errorStr, e);
        }

        if (status != 200) {
            return handleError(httpURLConnection);
        }

        return readResult(httpURLConnection);
    }

    private String handleError(HttpURLConnection urlConnection) throws WeiboException {

        String result = readError(urlConnection);
        String err = null;
        int errCode = 0;
        try {
            JSONObject json = new JSONObject(result);
            err = json.optString("error_description", "");
            if (TextUtils.isEmpty(err))
                err = json.getString("error");
            errCode = json.getInt("error_code");
            WeiboException exception = new WeiboException();
            exception.setError_code(errCode);
            exception.setOriError(err);
            throw exception;

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return result;
    }

    private String readResult(HttpURLConnection urlConnection) throws WeiboException {
        InputStream is = null;
        BufferedReader buffer = null;
        GlobalContext globalContext = GlobalContext.getInstance();
        String errorStr = globalContext.getString(R.string.timeout);
        globalContext = null;
        try {
            is = urlConnection.getInputStream();

            String content_encode = urlConnection.getContentEncoding();

            if (null != content_encode && !"".equals(content_encode) && content_encode.equals("gzip")) {
                is = new GZIPInputStream(is);
            }

            buffer = new BufferedReader(new InputStreamReader(is));
            StringBuilder strBuilder = new StringBuilder();
            String line;
            while ((line = buffer.readLine()) != null) {
                strBuilder.append(line);
            }

            return strBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new WeiboException(errorStr, e);
        } finally {
            Utility.closeSilently(is);
            Utility.closeSilently(buffer);
            urlConnection.disconnect();
        }

    }

    private String readError(HttpURLConnection urlConnection) throws WeiboException {
        InputStream is = null;
        BufferedReader buffer = null;
        GlobalContext globalContext = GlobalContext.getInstance();
        String errorStr = globalContext.getString(R.string.timeout);
        globalContext = null;
        try {
            is = urlConnection.getErrorStream();

            String content_encode = urlConnection.getContentEncoding();

            if (null != content_encode && !"".equals(content_encode) && content_encode.equals("gzip")) {
                is = new GZIPInputStream(is);
            }

            buffer = new BufferedReader(new InputStreamReader(is));
            StringBuilder strBuilder = new StringBuilder();
            String line;
            while ((line = buffer.readLine()) != null) {
                strBuilder.append(line);
            }

            return strBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new WeiboException(errorStr, e);
        } finally {
            Utility.closeSilently(is);
            Utility.closeSilently(buffer);
            urlConnection.disconnect();
        }

    }

    public String doGet(String urlStr, Map<String, String> param) throws WeiboException {
        GlobalContext globalContext = GlobalContext.getInstance();
        String errorStr = globalContext.getString(R.string.timeout);
        globalContext = null;
        InputStream is = null;
        try {

            StringBuilder urlBuilder = new StringBuilder(urlStr);
            urlBuilder.append("?").append(Utility.encodeUrl(param));
            URL url = new URL(urlBuilder.toString());

            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(8000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConnection.connect();

            return handleResponse(urlConnection);
        } catch (IOException e) {
            e.printStackTrace();
            throw new WeiboException(errorStr, e);
        }


    }

    public boolean doGetSaveFile(String urlStr, String path, FileDownloaderHttpHelper.DownloadListener downloadListener) {

        File file = FileManager.createNewFileInSDCard(path);
        if (file == null) {
            return false;
        }

        FileOutputStream out = null;
        InputStream in = null;
        HttpURLConnection urlConnection = null;
        try {

            URL url = new URL(urlStr);

            urlConnection = (HttpURLConnection) url
                    .openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(8000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConnection.connect();

            int status = urlConnection.getResponseCode();

            if (status != 200) {
                return false;
            }


            int bytetotal = (int) urlConnection.getContentLength();
            int bytesum = 0;
            int byteread = 0;
            out = new FileOutputStream(file);
            in = urlConnection.getInputStream();

            final Thread thread = Thread.currentThread();
            byte[] buffer = new byte[1444];
            while ((byteread = in.read(buffer)) != -1) {
                if (thread.isInterrupted()) {
                    file.delete();
                    throw new InterruptedIOException();
                }

                bytesum += byteread;
                out.write(buffer, 0, byteread);
                if (downloadListener != null && bytetotal > 0) {
                    downloadListener.pushProgress(bytesum, bytetotal);
                }
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utility.closeSilently(in);
            Utility.closeSilently(out);
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return false;
    }

    private static String getBoundry() {
        StringBuffer _sb = new StringBuffer();
        for (int t = 1; t < 12; t++) {
            long time = System.currentTimeMillis() + t;
            if (time % 3 == 0) {
                _sb.append((char) time % 9);
            } else if (time % 3 == 1) {
                _sb.append((char) (65 + time % 26));
            } else {
                _sb.append((char) (97 + time % 26));
            }
        }
        return _sb.toString();
    }

    public boolean doUploadFile(String urlStr, Map<String, String> param, String path, final FileUploaderHttpHelper.ProgressListener listener) throws WeiboException {
        String BOUNDARYSTR = getBoundry();
        String BOUNDARY = "--" + BOUNDARYSTR + "\r\n";
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        FileInputStream fis = null;
        GlobalContext globalContext = GlobalContext.getInstance();
        String errorStr = globalContext.getString(R.string.timeout);
        globalContext = null;
        InputStream is = null;
        try {
            URL url = null;

            url = new URL(urlStr);

            urlConnection = (HttpURLConnection) url
                    .openConnection();

            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(8000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-type", "multipart/form-data;boundary=" + BOUNDARYSTR);
            urlConnection.connect();

            out = new BufferedOutputStream(urlConnection.getOutputStream());

            StringBuilder sb = new StringBuilder();

            Map<String, String> paramMap = new HashMap<String, String>();

            for (String key : param.keySet()) {
                if (param.get(key) != null) {
                    paramMap.put(key, param.get(key));
                }
            }

            for (String str : paramMap.keySet()) {
                sb.append(BOUNDARY);
                sb.append("Content-Disposition:form-data;name=\"");
                sb.append(str);
                sb.append("\"\r\n\r\n");
                sb.append(param.get(str));
                sb.append("\r\n");
            }

            out.write(sb.toString().getBytes());

            File file = new File(path);
            out.write(BOUNDARY.getBytes());
            StringBuilder filenamesb = new StringBuilder();
            filenamesb.append("Content-Disposition:form-data;Content-Type:application/octet-stream;name=\"pic");
            filenamesb.append("\";filename=\"");
            filenamesb.append(file.getName() + "\"\r\n\r\n");
            out.write(filenamesb.toString().getBytes());

            fis = new FileInputStream(file);

            int bytesRead;
            int bytesAvailable;
            int bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024;

            bytesAvailable = fis.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = fis.read(buffer, 0, bufferSize);
            long transferred = 0;
            while (bytesRead > 0) {
                out.write(buffer, 0, bufferSize);
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fis.read(buffer, 0, bufferSize);
                transferred += bytesRead;
                if (listener != null)
                    listener.transferred(transferred);
            }

            out.write("\r\n\r\n".getBytes());
            fis.close();

            out.write(("--" + BOUNDARYSTR + "--\r\n").getBytes());
            out.flush();
            out.close();
            int status = urlConnection.getResponseCode();

            if (status != 200) {
                String error = handleError(urlConnection);
                throw new WeiboException(error);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new WeiboException(errorStr, e);
        } finally {
            Utility.closeSilently(fis);
            Utility.closeSilently(out);
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return true;
    }

}



