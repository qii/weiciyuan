package org.qii.weiciyuan.dao;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.domain.TimeLineMsgList;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-28
 * Time: 下午9:55
 * To change this template use File | Settings | File Templates.
 */
public class TimeLineFriendsMsg {


    private String getMsgs(String token) {
        String msg = "";

        String url = URLHelper.getFriendsTimeLine();

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", token);

        msg = HttpUtility.getInstance().execute(HttpMethod.Get, url, map);

        return msg;
    }


//    public List<Map<String, String>> getMsgList() {
//        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
//        String msg = getMsgs();
//
//        try {
//            JSONObject jsonObject = new JSONObject(msg);
//            JSONArray statuses = jsonObject.getJSONArray("statuses");
//            int length = statuses.length();
//            for (int i = 0; i < length; i++) {
//                JSONObject object = statuses.getJSONObject(i);
//                Map<String, String> map = new HashMap<String, String>();
//                map.put("id", object.optString("id"));
//                map.put("text", object.optString("text"));
//                Iterator iterator = object.keys();
//                String key;
//                while (iterator.hasNext()) {
//                    key = (String) iterator.next();
//                    Object value = object.opt(key);
//                    if (value instanceof String) {
//                        map.put(key, value.toString());
//                    } else if (value instanceof JSONObject) {
//                        map.put("screen_name", ((JSONObject) value).optString("screen_name"));
//                    }
//                }
//                list.add(map);
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
//        return list;
//    }

    public TimeLineMsgList getGSONMsgList(String token) {
        String json = getMsgs(token);
        Gson gson = new Gson();


        TimeLineMsgList value = null;
        try {
            value = gson.fromJson(json, TimeLineMsgList.class);
        } catch (JsonSyntaxException e) {
            Log.e("gson", "------------------------------");
            Log.e("gson", json);
        }

        return value;
    }
}
