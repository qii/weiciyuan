package org.qii.weiciyuan.dao;

import org.qii.weiciyuan.support.utils.GlobalContext;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-28
 * Time: 下午7:17
 * To change this template use File | Settings | File Templates.
 */
public class HomeLineMsg {

    public List getMsgs() {

        String token = GlobalContext.getInstance().getToken();

        String url = URLHelper.getHomeLine();

        return Collections.emptyList();
    }
}
