package org.qii.weiciyuan.dao.shorturl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.debug.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 13-2-21
 */
public class ShareShortUrlCountDao {
    private String token;
    private String shortUrl;

    public ShareShortUrlCountDao(String token, String shortUrl) {
        this.token = token;
        this.shortUrl = shortUrl;
    }

    public int getCount() throws WeiboException {
        String url = URLHelper.SHORT_URL_SHARE_COUNT;
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", token);
        map.put("url_short", shortUrl);

        String json = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.optJSONArray("urls");
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            return jsonObject1.optInt("share_counts", 0);
        } catch (JSONException e) {
            AppLogger.e(e.getMessage());
        }
        return 0;
    }
}
