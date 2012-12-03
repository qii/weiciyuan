package org.qii.weiciyuan.support.http;


import android.text.TextUtils;
import ch.boye.httpclientandroidlib.*;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.CookieStore;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.HttpRequestRetryHandler;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.methods.HttpRequestBase;
import ch.boye.httpclientandroidlib.client.protocol.ClientContext;
import ch.boye.httpclientandroidlib.client.utils.URIBuilder;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.conn.params.ConnRoutePNames;
import ch.boye.httpclientandroidlib.conn.scheme.PlainSocketFactory;
import ch.boye.httpclientandroidlib.conn.scheme.Scheme;
import ch.boye.httpclientandroidlib.conn.scheme.SchemeRegistry;
import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;
import ch.boye.httpclientandroidlib.impl.client.BasicCookieStore;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.impl.client.cache.CacheConfig;
import ch.boye.httpclientandroidlib.impl.conn.PoolingClientConnectionManager;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.params.CoreProtocolPNames;
import ch.boye.httpclientandroidlib.params.HttpConnectionParams;
import ch.boye.httpclientandroidlib.protocol.BasicHttpContext;
import ch.boye.httpclientandroidlib.protocol.HttpContext;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileUploaderHttpHelper;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpUtility {

    private static HttpUtility httpUtility = new HttpUtility();
    private HttpClient httpClient = null;


    private HttpUtility() {

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(
                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));


        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
        connectionManager.setMaxTotal(9);

        DefaultHttpClient backend = new DefaultHttpClient(connectionManager);

        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

            public boolean retryRequest(
                    IOException exception,
                    int executionCount,
                    HttpContext context) {
                if (executionCount >= AppConfig.RETRY_TIMES) {
                    // Do not retry if over max retry count
                    return false;
                }
//                if (exception instanceof InterruptedIOException) {
//                    // Timeout
//                    return false;
//                }
//                if (exception instanceof UnknownHostException) {
//                    // Unknown host
//                    return false;
//                }
//                if (exception instanceof ConnectException) {
//                    // Connection refused
//                    return false;
//                }
//                if (exception instanceof SSLException) {
//                    // SSL handshake exception
//                    return false;
//                }
//                HttpRequest request = (HttpRequest) context.getAttribute(
//                        ExecutionContext.HTTP_REQUEST);
//                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
//                if (idempotent) {
//                    // Retry if the request is considered idempotent
//                    return true;
//                }
//                return false;
                return true;
            }

        };

        backend.setHttpRequestRetryHandler(myRetryHandler);

        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxCacheEntries(1000);
        cacheConfig.setMaxObjectSize(8192);

        //4.2 N7 pad has bug, occur time out frequently
        //httpClient = new CachingHttpClient(new DecompressingHttpClient(backend), cacheConfig);

//        httpClient = new DecompressingHttpClient(new CachingHttpClient(backend, cacheConfig));
        httpClient = backend;
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 5000);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), 8000);

        httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);


    }

    public static HttpUtility getInstance() {
        HttpHost host = getProxySetting();
        if (host != null) {
            httpUtility.httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
        }
        return httpUtility;
    }


    public String executeNormalTask(HttpMethod httpMethod, String url, Map<String, String> param) throws WeiboException {
        switch (httpMethod) {
            case Post:
                return doPost(url, param);
            case Get:
                return doGet(url, param);

        }
        return "";
    }

    public boolean executeDownloadTask(String url, String path, FileDownloaderHttpHelper.DownloadListener downloadListener) {
        return doGetSaveFile(url, path, downloadListener);
    }

    public boolean executeUploadTask(String url, Map<String, String> param, String path, FileUploaderHttpHelper.ProgressListener listener) {
        return FileUploaderHttpHelper.upload(httpClient, url, param, path, listener);
    }

    private String doPost(String url, Map<String, String> param) throws WeiboException {
        AppLogger.d(url);
        HttpPost httpPost = new HttpPost();

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        Set<String> keys = param.keySet();
        for (String key : keys) {
            String value = param.get(key);
            if (!TextUtils.isEmpty(value))
                nameValuePairs.add(new BasicNameValuePair(key, value));

        }

        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {

        }
        try {
            httpPost.setURI(new URI(url));
        } catch (URISyntaxException e) {
            AppLogger.e(e.getMessage());
        }
        httpPost.setEntity(entity);

        HttpResponse response = getHttpResponse(httpPost, null);

        if (response != null) {
            String result = handleResponse(response);
            httpPost.releaseConnection();
            return result;
        } else {
            httpPost.abort();
            httpPost.releaseConnection();
            return "";
        }
    }

    /**
     * don't need error message to show
     */
    private boolean doGetSaveFile(String url, String path, FileDownloaderHttpHelper.DownloadListener downloadListener) {

        URIBuilder uriBuilder;
        HttpGet httpGet = new HttpGet();
        try {
            uriBuilder = new URIBuilder(url);
            httpGet.setURI(uriBuilder.build());
            AppLogger.d(uriBuilder.build().toString());
        } catch (URISyntaxException e) {
            AppLogger.d(e.getMessage());
        }

        HttpResponse response = null;
        try {

            response = httpClient.execute(httpGet);
            return FileDownloaderHttpHelper.saveFile(httpGet, response, path, downloadListener);

        } catch (Exception ignored) {

            AppLogger.e(ignored.getMessage());
            httpGet.abort();

        } finally {
            httpGet.releaseConnection();
        }

        return false;

    }

    private String doGet(String url, Map<String, String> param) throws WeiboException {

        HttpGet httpGet = new HttpGet();
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(url);
            Set<String> keys = param.keySet();
            for (String key : keys) {
                String value = param.get(key);
                if (!TextUtils.isEmpty(value))
                    uriBuilder.addParameter(key, param.get(key));
            }

            httpGet.setURI(uriBuilder.build());

            AppLogger.d(uriBuilder.build().toString());

        } catch (URISyntaxException e) {
            AppLogger.d(e.getMessage());
        }


        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpResponse response = getHttpResponse(httpGet, localContext);

        if (response != null) {
            String result = handleResponse(response);
            httpGet.releaseConnection();
            return result;
        } else {
            httpGet.abort();
            httpGet.releaseConnection();
            return "";
        }

    }


    private HttpResponse getHttpResponse(HttpRequestBase httpRequest, HttpContext localContext) throws WeiboException {
        HttpResponse response = null;
        try {
            if (localContext != null) {
                response = httpClient.execute(httpRequest, localContext);
            } else {
                response = httpClient.execute(httpRequest);
            }
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            AppLogger.e(e.getMessage());
            httpRequest.abort();
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.timeout), e);

        } catch (ClientProtocolException e) {
            AppLogger.e(e.getMessage());
            httpRequest.abort();
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.timeout), e);

        } catch (IOException e) {
            AppLogger.e(e.getMessage());
            httpRequest.abort();
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.timeout), e);
        }
        return response;
    }

    private String handleResponse(HttpResponse httpResponse) throws WeiboException {

        StatusLine status = httpResponse.getStatusLine();
        int statusCode = status.getStatusCode();

        if (statusCode != HttpStatus.SC_OK) {
            return handleError(httpResponse);
        }

        return readResult(httpResponse);
    }


    private String readResult(HttpResponse response) throws WeiboException {
        HttpEntity entity = response.getEntity();
        String result = "";

        try {
            AppLogger.d(String.valueOf(entity.getContentLength()));
            result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } catch (IOException e) {
            AppLogger.e(e.getMessage());
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.timeout), e);
        }

        AppLogger.d(result);


        return result;
    }

    private String handleError(HttpResponse httpResponse) throws WeiboException {

        StatusLine status = httpResponse.getStatusLine();
        int statusCode = status.getStatusCode();

        String result = "";

        if (statusCode != HttpStatus.SC_OK) {

            result = readResult(httpResponse);
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

        }

        return result;
    }


    private static HttpHost getProxySetting() {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (!TextUtils.isEmpty(proxyHost) && !TextUtils.isEmpty(proxyPort)) {
            return new HttpHost(proxyHost, Integer.valueOf(proxyPort));
        } else {
            return null;
        }
    }
}

