package org.qii.weiciyuan.dao;

import android.text.TextUtils;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午1:17
 * To change this template use File | Settings | File Templates.
 */
public class CommentsTimeLineMsgDao {

    private String access_token;

    private String id;
    private String since_id;
    private String max_id;
    private String count;
    private String page;
    private String filter_by_author;

    public CommentsTimeLineMsgDao(String access_token) {
        if (TextUtils.isEmpty(access_token))
            throw new IllegalArgumentException();
        this.access_token = access_token;
    }


}
