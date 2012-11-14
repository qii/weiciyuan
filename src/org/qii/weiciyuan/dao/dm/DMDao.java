package org.qii.weiciyuan.dao.dm;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.DMUserListBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMDao {

    private String access_token;

    public DMDao(String token) {
        this.access_token = token;
    }

    public DMUserListBean getUserList() throws WeiboException {
        String url = URLHelper.DM_USERLIST;
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);
        DMUserListBean value = null;
        try {
            value = new Gson().fromJson(jsonData, DMUserListBean.class);
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage());

        }
        return value;
    }

}
