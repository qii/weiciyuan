package org.qii.weiciyuan.ui.maintimeline;

import org.qii.weiciyuan.bean.GroupListBean;
import org.qii.weiciyuan.dao.maintimeline.FriendGroupDao;
import org.qii.weiciyuan.support.database.GroupDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-12-28
 */
public class GroupInfoTask extends MyAsyncTask<Void, GroupListBean, GroupListBean> {


    private WeiboException e;

    private String token;

    public GroupInfoTask(String token) {
        this.token = token;
    }

    @Override
    protected GroupListBean doInBackground(Void... params) {
        try {
            return new FriendGroupDao(token).getGroup();
        } catch (WeiboException e) {
            this.e = e;
            cancel(true);
        }
        return null;
    }


    @Override
    protected void onPostExecute(GroupListBean groupListBean) {
        GroupDBTask.update(groupListBean, GlobalContext.getInstance().getCurrentAccountId());
        GlobalContext.getInstance().setGroup(groupListBean);

        super.onPostExecute(groupListBean);
    }

}