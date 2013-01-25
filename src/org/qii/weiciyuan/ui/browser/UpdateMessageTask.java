package org.qii.weiciyuan.ui.browser;

import android.app.Activity;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.show.ShowStatusDao;
import org.qii.weiciyuan.support.error.ErrorCode;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

/**
 * User: qii
 * Date: 13-1-25
 */
public class UpdateMessageTask extends MyAsyncTask<Void, Void, MessageBean> {
    private MessageBean msg;
    private TextView content;
    private TextView recontent;
    private BrowserWeiboMsgFragment fragment;
    private WeiboException e;

    public UpdateMessageTask(BrowserWeiboMsgFragment fragment, TextView content, TextView recontent, MessageBean msg) {
        this.fragment = fragment;
        this.content = content;
        this.recontent = recontent;
        this.msg = msg;
    }

    private Activity getActivity() {
        return fragment.getActivity();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


    }

    @Override
    protected MessageBean doInBackground(Void... params) {
        try {
            return new ShowStatusDao(GlobalContext.getInstance().getSpecialToken(), msg.getId()).getMsg();
        } catch (WeiboException e) {
            this.e = e;
            cancel(true);
        }
        return null;
    }

    @Override
    protected void onCancelled(MessageBean weiboMsgBean) {
        super.onCancelled(weiboMsgBean);
        if (Utility.isAllNotNull(getActivity(), this.e)) {
            Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            if (e.getError_code() == ErrorCode.DELETED) {
                setTextViewDeleted();
            }
        }
    }

    @Override
    protected void onPostExecute(MessageBean newValue) {
        if (newValue != null && e == null) {
            if (isStatusDeleted(newValue)) {
                setTextViewDeleted(content);
                if (recontent.getVisibility() == View.VISIBLE) {
                    setTextViewDeleted(recontent);
                }
            } else if (isRepostDeleted(newValue)) {
                setTextViewDeleted(recontent);
            } else {
                msg = newValue;
                fragment.buildViewData();
                Intent intent = new Intent();
                intent.putExtra("msg", msg);
                getActivity().setResult(0, intent);
            }
        }
        super.onPostExecute(newValue);
    }

    //sometime status is deleted
    private boolean isStatusDeleted(MessageBean newValue) {

        //status is deleted
        if ((msg != null))
            if ((msg.getUser() != null) && (newValue.getUser() == null)) {
                return true;
            }

        return false;

    }


    //sometime the ori status is deleted
    private boolean isRepostDeleted(MessageBean newValue) {

        if (msg.getRetweeted_status() != null && msg.getRetweeted_status().getUser() != null) {

            //ori status is deleted
            if (newValue.getRetweeted_status() != null && newValue.getRetweeted_status().getUser() == null) {
                return true;
            }
        }

        return false;

    }

    private void setTextViewDeleted() {
        SpannableString ss = SpannableString.valueOf(content.getText());
        ss.setSpan(new StrikethroughSpan(), 0, ss.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        content.setText(ss);
    }

    private void setTextViewDeleted(TextView tv) {
        SpannableString ss = SpannableString.valueOf(tv.getText());
        ss.setSpan(new StrikethroughSpan(), 0, ss.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ss);
    }
}