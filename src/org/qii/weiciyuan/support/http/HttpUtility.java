package org.qii.weiciyuan.support.http;


import android.text.TextUtils;
import ch.boye.httpclientandroidlib.*;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.CookieStore;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.methods.HttpRequestBase;
import ch.boye.httpclientandroidlib.client.protocol.ClientContext;
import ch.boye.httpclientandroidlib.client.utils.URIBuilder;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.impl.client.BasicCookieStore;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.impl.conn.PoolingClientConnectionManager;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.CoreProtocolPNames;
import ch.boye.httpclientandroidlib.params.HttpConnectionParams;
import ch.boye.httpclientandroidlib.params.HttpParams;
import ch.boye.httpclientandroidlib.protocol.BasicHttpContext;
import ch.boye.httpclientandroidlib.protocol.HttpContext;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.utils.ActivityUtils;
import org.qii.weiciyuan.support.utils.AppLogger;

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
    private HttpGet httpGet = new HttpGet();
    private HttpPost httpPost = new HttpPost();

    private HttpUtility() {

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        //default 2 connections
        httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 4000);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), 4000);


    }

    public static HttpUtility getInstance() {

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

    public String executeDownloadTask(String url, String path) {
        return doGetSaveFile(url, path);
    }

    private String doPost(String url, Map<String, String> param) throws WeiboException {
        AppLogger.d(url);
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

            return dealWithResponse(response);
        } else {
            return "";
        }
    }

    /**
     * don't need error message to show
     *
     * @param url
     * @param path
     * @return
     */
    private String doGetSaveFile(String url, String path) {

        URIBuilder uriBuilder;
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

        } catch (ConnectTimeoutException ignored) {

            AppLogger.e(ignored.getMessage());

        } catch (ClientProtocolException ignored) {
            AppLogger.e(ignored.getMessage());

        } catch (IOException ignored) {
            AppLogger.e(ignored.getMessage());
        }


        if (response != null) {

            return FileDownloaderHttpHelper.saveFile(response, path);

        } else {
            return "";
        }
    }

    private String doGet(String url, Map<String, String> param) throws WeiboException {


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
            return dealWithResponse(response);
        } else {
            return "";
        }

    }


    private HttpResponse getHttpResponse(HttpRequestBase httpRequest, HttpContext localContext) {
        HttpResponse response = null;
        try {
            if (localContext != null) {
                response = httpClient.execute(httpRequest, localContext);
            } else {
                response = httpClient.execute(httpRequest);
            }
        } catch (ConnectTimeoutException e) {

            AppLogger.e(e.getMessage());
            ActivityUtils.showTips(R.string.timeout);

        } catch (ClientProtocolException e) {
            AppLogger.e(e.getMessage());
            ActivityUtils.showTips(R.string.timeout);

        } catch (IOException e) {
            AppLogger.e(e.getMessage());
            ActivityUtils.showTips(R.string.timeout);
        }
        return response;
    }

    private String dealWithResponse(HttpResponse httpResponse) throws WeiboException {


        StatusLine status = httpResponse.getStatusLine();
        int statusCode = status.getStatusCode();


        if (statusCode != HttpStatus.SC_OK) {
            return dealWithError(httpResponse);
        }


        return readResult(httpResponse);


    }


    private String readResult(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        String result = "";

        try {
            result = EntityUtils.toString(entity);

        } catch (IOException e) {

            AppLogger.e(e.getMessage());
            ActivityUtils.showTips(R.string.timeout);
        }

        AppLogger.d(result);


        return result;
    }

    private String dealWithError(HttpResponse httpResponse) throws WeiboException {

        StatusLine status = httpResponse.getStatusLine();
        int statusCode = status.getStatusCode();

        String result = "";

        if (statusCode != HttpStatus.SC_OK) {

            result = readResult(httpResponse);
            String err = null;
            int errCode = 0;
            try {
                JSONObject json = new JSONObject(result);
                err = json.getString("error");
                errCode = json.getInt("error_code");
                WeiboException exception = new WeiboException();
                exception.setError_code(errCode);
                throw exception;

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return result;
    }


}

