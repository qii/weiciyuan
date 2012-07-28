package org.qii.weiciyuan.dao;

import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.weibo.Token;
import org.qii.weiciyuan.weibo.Utility;
import org.qii.weiciyuan.weibo.WeiboException;
import org.qii.weiciyuan.weibo.WeiboParameters;

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
        Token token = new Token();
        token.setToken(GlobalContext.getInstance().getToken());
        token.setExpiresIn(GlobalContext.getInstance().getExpires());

        String url = URLHelper.getMentionsTimeLine();
        WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", GlobalContext.getInstance().getToken());
        try {
            String str = Utility.openUrl(GlobalContext.getInstance(), url, "GET", bundle, token);
            return str;
        } catch (WeiboException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";
    }
}
