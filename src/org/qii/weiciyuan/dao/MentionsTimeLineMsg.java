package org.qii.weiciyuan.dao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.weibo.Token;
import org.qii.weiciyuan.weibo.Utility;
import org.qii.weiciyuan.weibo.WeiboException;
import org.qii.weiciyuan.weibo.WeiboParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-28
 * Time: 下午10:04
 * To change this template use File | Settings | File Templates.
 */
public class MentionsTimeLineMsg implements TimeLineMsg {
    @Override
    public String getMsgs() {
        String msg = "";
        Token token = new Token();
        token.setToken(GlobalContext.getInstance().getToken());
        token.setExpiresIn(GlobalContext.getInstance().getExpires());

        String url = URLHelper.getMentionsTimeLine();
        WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", GlobalContext.getInstance().getToken());
        try {
            msg = Utility.openUrl(GlobalContext.getInstance(), url, "GET", bundle, token);

        } catch (WeiboException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return msg;
    }


    public List<Map<String, String>> getMsgList() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        String msg = getMsgs();

        try {
            JSONObject jsonObject = new JSONObject(msg);
            JSONArray statuses = jsonObject.getJSONArray("statuses");
            int length = statuses.length();
            for (int i = 0; i < length; i++) {
                JSONObject object = statuses.getJSONObject(i);
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", object.optString("id"));
                map.put("text", object.optString("text"));
                list.add(map);
            }

        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return list;
    }
}
