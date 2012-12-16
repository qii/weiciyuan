package org.qii.weiciyuan.dao.maintimeline;

import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.support.error.WeiboException;

/**
 * User: qii
 * Date: 12-12-16
 */
public interface ICommentsTimeLineDao {
    public CommentListBean getGSONMsgList() throws WeiboException;

    public void setSince_id(String since_id);

    public void setMax_id(String max_id);
}
