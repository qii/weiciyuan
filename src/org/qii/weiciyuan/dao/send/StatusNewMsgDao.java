package org.qii.weiciyuan.dao.send;

import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.http.URLManager;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-7-29
 */
public class StatusNewMsgDao {

    private String access_token;

    public StatusNewMsgDao(String access_token) {

        this.access_token = access_token;
    }

    public void sendNewMsg(String str) {
        String url = URLManager.getRealUrl("update");
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("status", str);

        try {
            HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, url, map);
        } catch (WeiboException e) {
            e.printStackTrace();
        }

    }
}
