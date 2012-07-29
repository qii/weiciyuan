package org.qii.weiciyuan.support.http;


import ch.boye.httpclientandroidlib.HttpVersion;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.utils.URIUtils;
import ch.boye.httpclientandroidlib.client.utils.URLEncodedUtils;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.CoreProtocolPNames;
import ch.boye.httpclientandroidlib.params.HttpParams;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 上午10:10
 * To change this template use File | Settings | File Templates.
 */
public class HttpUtility {

    private static HttpUtility httpUtility = new HttpUtility();
    private HttpClient httpclient = null;
    private HttpGet httpGet = new HttpGet();
    private HttpPost httpPost = new HttpPost();

    private HttpUtility() {

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        httpclient = new DefaultHttpClient(params);


    }

    public static HttpUtility getInstance() {

        return httpUtility;
    }


    public String execute(HttpMethod httpMethod, String url, Map<String, String> param) {
        switch (httpMethod) {
            case Post:
                return doPost(url, param);
            case Get:
                try {
                    return doGet(url, param);
                } catch (URISyntaxException e) {

                }
        }
        return "";
    }

    public String doPost(String url, Map<String, String> param) {
        return "";
    }

    public String doGet(String url, Map<String, String> param) throws URISyntaxException {

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("q", "httpclient"));
        qparams.add(new BasicNameValuePair("btnG", "Google Search"));
        qparams.add(new BasicNameValuePair("aq", "f"));
        qparams.add(new BasicNameValuePair("oq", null));
        URI uri = URIUtils.createURI("http", "www.google.com", -1, "/search",
                URLEncodedUtils.format(qparams, "UTF-8"), null);
        httpGet.setURI(uri);

        return "";
    }
}

