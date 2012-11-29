package org.qii.weiciyuan.dao.login;

import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-11-29
 * sina weibo dont allow third apps use this api, the result is always error
 */
public class RefreshOAuthDao {

    public String refreshToken() throws WeiboException {
        String url = URLHelper.OAUTH2_ACCESS_TOKEN;
        Map<String, String> map = new HashMap<String, String>();
        map.put("code", code);
        map.put("redirect_uri", redirect_uri);
        map.put("client_id", client_id);
        map.put("client_secret", client_secret);
        map.put("grant_type", grant_type);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, url, map);


        if ((jsonData != null) && (jsonData.contains("{"))) {
            try {
                JSONObject localJSONObject = new JSONObject(jsonData);
                return localJSONObject.optString("access_token");
            } catch (JSONException localJSONException) {

            }

        }
        return "";

    }

    public RefreshOAuthDao(String code) {
        this.code = code;
    }

    private String code;
    private String redirect_uri = URLHelper.DIRECT_URL;
    private String client_id = URLHelper.APP_KEY;
    private String client_secret = URLHelper.APP_SECRET;
    private String grant_type = "authorization_code";

}
