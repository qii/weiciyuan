package org.qii.weiciyuan.ui.send;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.ItemBean;
import org.qii.weiciyuan.dao.send.ReplyToCommentMsgDao;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.support.database.DraftDBManager;
import org.qii.weiciyuan.support.database.draftbean.ReplyDraftBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.search.AtUserActivity;

/**
 * User: qii
 * Date: 12-8-28
 */
public class WriteReplyToCommentActivity extends AbstractWriteActivity<CommentBean> {

    private CommentBean bean;
    private ReplyDraftBean replyDraftBean;
    private MenuItem enableRepost;
    private boolean savedEnableRepost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(getString(R.string.reply_to_comment));
        getActionBar().setSubtitle(GlobalContext.getInstance().getCurrentAccountName());

        token = getIntent().getStringExtra("token");
        if (TextUtils.isEmpty(token))
            token = GlobalContext.getInstance().getSpecialToken();

        bean = (CommentBean) getIntent().getSerializableExtra("msg");
        if (bean == null) {
            replyDraftBean = (ReplyDraftBean) getIntent().getSerializableExtra("draft");
            getEditTextView().setText(replyDraftBean.getContent());
            bean = replyDraftBean.getCommentBean();
        }

        getEditTextView().setHint("@" + bean.getUser().getScreen_name() + "：" + bean.getText());

        //this time menu item is null...omg fuck android
        if (savedInstanceState != null) {
            savedEnableRepost = savedInstanceState.getBoolean("repost");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("repost", enableRepost.isChecked());
    }

    @Override
    protected boolean canShowSaveDraftDialog() {
        if (replyDraftBean == null) {
            return true;
        } else if (!replyDraftBean.getContent().equals(getEditTextView().getText().toString())) {
            return true;
        }
        return false;
    }

    @Override
    public void saveToDraft() {
        if (!TextUtils.isEmpty(getEditTextView().getText().toString())) {
            DraftDBManager.getInstance().insertReply(getEditTextView().getText().toString(), bean, GlobalContext.getInstance().getCurrentAccountId());
        }
        finish();
    }

    @Override
    protected void removeDraft() {
        if (replyDraftBean != null)
            DraftDBManager.getInstance().remove(replyDraftBean.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_commentnewactivity, menu);
        menu.findItem(R.id.menu_enable_ori_comment).setVisible(false);
        menu.findItem(R.id.menu_enable_repost).setVisible(true);
        enableRepost = menu.findItem(R.id.menu_enable_repost);
        enableRepost.setChecked(savedEnableRepost);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                finish();
                break;
            case R.id.menu_enable_repost:
                if (enableRepost.isChecked()) {
                    enableRepost.setChecked(false);
                } else {
                    enableRepost.setChecked(true);
                }
                break;
            case R.id.menu_at:
                Intent intent = new Intent(WriteReplyToCommentActivity.this, AtUserActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, AT_USER);
                break;
        }
        return true;
    }

    @Override
    protected CommentBean sendData() throws WeiboException {
        ReplyToCommentMsgDao dao = new ReplyToCommentMsgDao(token, bean, ((EditText) findViewById(R.id.status_new_content)).getText().toString());
        CommentBean commentBean = dao.reply();
        if (enableRepost.isChecked()) {
            repost();
        }
        return commentBean;
    }


    private ItemBean repost() throws WeiboException {

        String content = ((EditText) findViewById(R.id.status_new_content)).getText().toString();
        String msgContent = "//@" + bean.getUser().getScreen_name() + ": " + bean.getText();
        String total = content + msgContent;
        if (total.length() < 140) {
            content = total;
        }

        RepostNewMsgDao dao = new RepostNewMsgDao(token, bean.getStatus().getId());

        dao.setStatus(content);

        return dao.sendNewMsg();
    }


    @Override
    protected boolean canSend() {

        boolean haveContent = !TextUtils.isEmpty(getEditTextView().getText().toString());
        boolean haveToken = !TextUtils.isEmpty(token);
        int sum = Utility.length(getEditTextView().getText().toString());
        int num = 140 - sum;

        boolean contentNumBelow140 = (num >= 0);

        if (haveContent && haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveContent && !haveToken) {
                Toast.makeText(this, getString(R.string.content_cant_be_empty_and_dont_have_account), Toast.LENGTH_SHORT).show();
            } else if (!haveContent) {
                getEditTextView().setError(getString(R.string.content_cant_be_empty));
            } else if (!haveToken) {
                Toast.makeText(this, getString(R.string.dont_have_account), Toast.LENGTH_SHORT).show();
            }

            if (!contentNumBelow140) {
                getEditTextView().setError(getString(R.string.content_words_number_too_many));
            }

        }

        return false;
    }


}
