package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.qii.weiciyuan.bean.android.MentionTimeLineData;
import org.qii.weiciyuan.support.database.MentionsTimeLineDBTask;

/**
 * User: qii
 * Date: 13-4-10
 */
public class MentionsWeiboTimeDBLoader extends AsyncTaskLoader<MentionTimeLineData> {

    private String accountId;

    public MentionsWeiboTimeDBLoader(Context context, String accountId) {
        super(context);
        this.accountId = accountId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    public MentionTimeLineData loadInBackground() {
        return MentionsTimeLineDBTask.getRepostLineMsgList(accountId);
    }

}
