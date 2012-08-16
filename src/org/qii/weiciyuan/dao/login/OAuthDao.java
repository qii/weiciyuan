package org.qii.weiciyuan.dao.login;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.http.URLManager;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-7-30
 * Time: 下午1:55
 */
public class OAuthDao {

    private String access_token;

    public OAuthDao(String access_token) {
        if (TextUtils.isEmpty(access_token))
            throw new IllegalArgumentException();
        this.access_token = access_token;
    }

    public UserBean getOAuthUserInfo() throws WeiboException {

        String uidJson = getOAuthUserUIDJsonData();
        String uid = "";

        try {
            JSONObject jsonObject = new JSONObject(uidJson);
            uid = jsonObject.optString("uid");
        } catch (JSONException e) {

        }


        Map<String, String> map = new HashMap<String, String>();
        map.put("uid", uid);
        map.put("access_token", access_token);

        String url = URLManager.getRealUrl("usershow");
        String result = null;

        result = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);


        Gson gson = new Gson();
        UserBean user = new UserBean();
        try {
            user = gson.fromJson(result, UserBean.class);
        } catch (JsonSyntaxException e) {
            AppLogger.e(result);
        }

        return user;
    }

    private String getOAuthUserUIDJsonData() {

        String url = URLManager.getRealUrl("uid");
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        try {
            return HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);
        } catch (WeiboException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

}
