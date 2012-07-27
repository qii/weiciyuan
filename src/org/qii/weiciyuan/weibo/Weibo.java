/*
 * Copyright 2011 Sina.
 *
 * Licensed under the Apache License and Weibo License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.open.weibo.com
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qii.weiciyuan.weibo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieSyncManager;


public class Weibo {

    public static String SERVER = "https://api.weibo.com/2/";

    public static String URL_OAUTH2_ACCESS_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";

    private static String APP_KEY = "";
    private static String APP_SECRET = "";

    private static Weibo mWeiboInstance = null;
    private Token mAccessToken = null;
    private RequestToken mRequestToken = null;

    private WeiboDialogListener mAuthDialogListener;

    private static final int DEFAULT_AUTH_ACTIVITY_CODE = 32973;

    public static final String TOKEN = "access_token";
    public static final String EXPIRES = "expires_in";

    private String mRedirectUrl;

    private Weibo() {
        Utility.setRequestHeader("Accept-Encoding", "gzip");
        Utility.setTokenObject(this.mRequestToken);
    }

    public synchronized static Weibo getInstance() {
        if (mWeiboInstance == null) {
            mWeiboInstance = new Weibo();
        }
        return mWeiboInstance;
    }

    // 设置accessToken
    public void setAccessToken(AccessToken token) {
        mAccessToken = token;
    }

    public Token getAccessToken() {
        return this.mAccessToken;
    }

    public void setupConsumerConfig(String consumer_key, String consumer_secret) {
        Weibo.APP_KEY = consumer_key;
        Weibo.APP_SECRET = consumer_secret;
    }

    public static String getAppKey() {
        return Weibo.APP_KEY;
    }

    public static String getAppSecret() {
        return Weibo.APP_SECRET;
    }


    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    public void setRedirectUrl(String mRedirectUrl) {
        this.mRedirectUrl = mRedirectUrl;
    }

    /**
     * Requst sina weibo open api by get or post
     *
     * @param url        Openapi request URL.
     * @param params     http get or post parameters . e.g.
     *                   gettimeling?max=max_id&min=min_id max and max_id is a pair of
     *                   key and value for params, also the min and min_id
     * @param httpMethod http verb: e.g. "GET", "POST", "DELETE"
     * @throws java.io.IOException
     * @throws java.net.MalformedURLException
     */
    public String request(Context context, String url, WeiboParameters params, String httpMethod,
                          Token token) throws WeiboException {
        String rlt = Utility.openUrl(context, url, httpMethod, params, this.mAccessToken);
        return rlt;
    }


    private void startDialogAuth(Activity activity, String[] permissions) {
        WeiboParameters params = new WeiboParameters();
        if (permissions.length > 0) {
            params.add("scope", TextUtils.join(",", permissions));
        }
        CookieSyncManager.createInstance(activity);
        dialog(activity, params, new WeiboDialogListener() {

            public void onComplete(Bundle values) {
                // ensure any cookies set by the dialog are saved
                CookieSyncManager.getInstance().sync();
                if (null == mAccessToken) {
                    mAccessToken = new Token();
                }
                mAccessToken.setToken(values.getString(TOKEN));
                mAccessToken.setExpiresIn(values.getString(EXPIRES));
                if (isSessionValid()) {
                    Log.d("Weibo-authorize",
                            "Login Success! access_token=" + mAccessToken.getToken() + " expires="
                                    + mAccessToken.getExpiresIn());
                    mAuthDialogListener.onComplete(values);
                } else {
                    Log.d("Weibo-authorize", "Failed to receive access token");
                    mAuthDialogListener.onWeiboException(new WeiboException(
                            "Failed to receive access token."));
                }
            }

            public void onError(DialogError error) {
                Log.d("Weibo-authorize", "Login failed: " + error);
                mAuthDialogListener.onError(error);
            }

            public void onWeiboException(WeiboException error) {
                Log.d("Weibo-authorize", "Login failed: " + error);
                mAuthDialogListener.onWeiboException(error);
            }

            public void onCancel() {
                Log.d("Weibo-authorize", "Login canceled");
                mAuthDialogListener.onCancel();
            }
        });
    }

    /**
     * User-Agent Flow
     *
     * @param activity
     * @param listener
     */
    public void authorize(Activity activity, final WeiboDialogListener listener) {
        Utility.setAuthorization(new Oauth2AccessTokenHeader());

        mAuthDialogListener = listener;

        startDialogAuth(activity, new String[]{});

    }


    public void dialog(Context context, WeiboParameters parameters,
                       final WeiboDialogListener listener) {
        parameters.add("client_id", APP_KEY);
        parameters.add("response_type", "token");
        parameters.add("redirect_uri", mRedirectUrl);
        parameters.add("display", "mobile");

        if (isSessionValid()) {
            parameters.add(TOKEN, mAccessToken.getToken());
        }
        String url = URL_OAUTH2_ACCESS_AUTHORIZE + "?" + Utility.encodeUrl(parameters);
        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
//            Utility.showAlert(context, "Error",
//                    "Application requires permission to access the Internet");
        } else {
            new WeiboDialog(this, context, url, listener).show();
        }
    }

    public boolean isSessionValid() {
        if (mAccessToken != null) {
            return (!TextUtils.isEmpty(mAccessToken.getToken()) && (mAccessToken.getExpiresIn() == 0 || (System
                    .currentTimeMillis() < mAccessToken.getExpiresIn())));
        }
        return false;
    }
}
