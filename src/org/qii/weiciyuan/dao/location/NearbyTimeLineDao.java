package org.qii.weiciyuan.dao.location;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.NearbyStatusListBean;
import org.qii.weiciyuan.dao.URLHelper;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.http.HttpMethod;
import org.qii.weiciyuan.support.http.HttpUtility;
import org.qii.weiciyuan.support.debug.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 13-3-8
 * http://open.weibo.com/wiki/2/place/nearby_timeline
 */
public class NearbyTimeLineDao {

    public NearbyStatusListBean get() throws WeiboException {

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("lat", lat);
        map.put("long", long_fix);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, URLHelper.NEARBY_STATUS, map);
        try {
            NearbyStatusListBean value = new Gson().fromJson(jsonData, NearbyStatusListBean.class);
            if (value != null)
                return value;
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage());
        }
        return null;
    }

    public NearbyTimeLineDao(String token, double lat, double long_fix) {
        this.access_token = token;
        this.lat = String.valueOf(lat);
        this.long_fix = String.valueOf(long_fix);
    }

    private String access_token;
    private String lat;
    private String long_fix;
    private String range;
    private String starttime;
    private String endtime;
    private String sort;
    private String count = "50";
    private String page;
    private String base_app;
    private String offset;
}
