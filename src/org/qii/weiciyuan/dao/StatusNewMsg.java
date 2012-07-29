package org.qii.weiciyuan.dao;

import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.http.URLManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午3:38
 * To change this template use File | Settings | File Templates.
 */
public class StatusNewMsg {

    public void sendNewMsg(String str) {
        String url = URLManager.getRealUrl("update");
        Map<String, String> map = new HashMap<String, String>();
        map.put("status", str);

        HttpUtility.getInstance().execute(HttpMethod.Post, url, map);

    }
}
