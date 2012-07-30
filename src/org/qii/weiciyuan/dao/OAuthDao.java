package org.qii.weiciyuan.dao;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.bean.WeiboUser;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.http.URLManager;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-7-30
 * Time: 下午1:55
 */
public class OAuthDao {

    public static WeiboUser getOAuthUserInfo(String token) {

        String uidJson = getOAuthUserUID(token);
        String uid="";

        try {
            JSONObject jsonObject=new JSONObject(uidJson);
            uid=jsonObject.optString("uid");
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        Map<String, String> map = new HashMap<String, String>();
        map.put("uid", uid);
        map.put("access_token", token);

        String url = URLManager.getRealUrl("usershow");
        String result = HttpUtility.getInstance().execute(HttpMethod.Get, url, map);

        Gson gson = new Gson();


        WeiboUser user = new WeiboUser();
        try {
            user = gson.fromJson(result, WeiboUser.class);
        } catch (JsonSyntaxException e) {
            Log.e("gson", "------------------------------");
            Log.e("gson", result);
        }

        return user;


    }

    private static String getOAuthUserUID(String token) {

        String url = URLManager.getRealUrl("uid");
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", token);
        return HttpUtility.getInstance().execute(HttpMethod.Get, url, map);
    }

}
