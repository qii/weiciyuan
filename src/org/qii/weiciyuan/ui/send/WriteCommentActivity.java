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
import org.qii.weiciyuan.bean.ItemBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.send.CommentNewMsgDao;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.support.database.DraftDBManager;
import org.qii.weiciyuan.support.database.draftbean.CommentDraftBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.search.AtUserActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class WriteCommentActivity extends AbstractWriteActivity<ItemBean> {

    private String token;
    private MessageBean msg;
    private CommentDraftBean commentDraftBean;

    private MenuItem enableCommentOri;
    private MenuItem enableRepost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(R.string.comments);
        getActionBar().setSubtitle(GlobalContext.getInstance().getCurrentAccountName());

        token = getIntent().getStringExtra("token");
        if (TextUtils.isEmpty(token))
            token = GlobalContext.getInstance().getSpecialToken();

        msg = (MessageBean) getIntent().getSerializableExtra("msg");
        if (msg == null) {
            commentDraftBean = (CommentDraftBean) getIntent().getSerializableExtra("draft");
            msg = commentDraftBean.getMessageBean();
            getEditTextView().setText(commentDraftBean.getContent());

        }

        getEditTextView().setHint("@" + msg.getUser().getScreen_name() + "：" + msg.getText());

    }

    @Override
    protected boolean canShowSaveDraftDialog() {
        if (commentDraftBean == null) {
            return true;
        } else if (!commentDraftBean.getContent().equals(getEditTextView().getText().toString())) {
            return true;
        }
        return false;
    }

    @Override
    public void saveToDraft() {
        if (!TextUtils.isEmpty(getEditTextView().getText().toString())) {
            DraftDBManager.getInstance().insertComment(getEditTextView().getText().toString(), msg, GlobalContext.getInstance().getCurrentAccountId());
        }
        finish();
    }

    @Override
    protected void removeDraft() {
        if (commentDraftBean != null)
            DraftDBManager.getInstance().remove(commentDraftBean.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_commentnewactivity, menu);
        enableCommentOri = menu.findItem(R.id.menu_enable_ori_comment);
        enableRepost = menu.findItem(R.id.menu_enable_repost);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (msg != null && msg.getRetweeted_status() != null) {
            enableCommentOri.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                finish();
                break;

            case R.id.menu_enable_ori_comment:
                if (enableCommentOri.isChecked()) {
                    enableCommentOri.setChecked(false);
                } else {
                    enableCommentOri.setChecked(true);
                }
                break;
            case R.id.menu_enable_repost:
                if (enableRepost.isChecked()) {
                    enableRepost.setChecked(false);
                } else {
                    enableRepost.setChecked(true);
                }
                break;
            case R.id.menu_at:
                Intent intent = new Intent(WriteCommentActivity.this, AtUserActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, AT_USER);
                break;
        }
        return true;
    }

    @Override
    protected ItemBean sendData() throws WeiboException {
        if (!enableRepost.isChecked()) {
            CommentNewMsgDao dao = new CommentNewMsgDao(token, msg.getId(), ((EditText) findViewById(R.id.status_new_content)).getText().toString());
            if (enableCommentOri.isChecked()) {
                dao.enableComment_ori(true);
            } else {
                dao.enableComment_ori(false);
            }

            return dao.sendNewMsg();
        } else {
            return repost();
        }

    }

    /**
     * 1. this message has repost's message
     * 2. this message is an original message
     * <p/>
     * if this message has repost's message,try to include its content,
     * if total word number above 140,discard current msg content
     */

    private ItemBean repost() throws WeiboException {

        String content = ((EditText) findViewById(R.id.status_new_content)).getText().toString();

        if (msg.getRetweeted_status() != null) {
            String msgContent = "//@" + msg.getUser().getScreen_name() + ": " + msg.getText();
            String total = content + msgContent;
            if (total.length() < 140) {
                content = total;
            }
        }

        RepostNewMsgDao dao = new RepostNewMsgDao(token, msg.getId());

        boolean comment = true;
        boolean oriComment = enableCommentOri.isChecked();

        if (comment && oriComment) {
            dao.setIs_comment(RepostNewMsgDao.ENABLE_COMMENT_ALL);
        } else if (comment) {
            dao.setIs_comment(RepostNewMsgDao.ENABLE_COMMENT);
        } else if (oriComment) {
            dao.setIs_comment(RepostNewMsgDao.ENABLE_ORI_COMMENT);
        }


        dao.setStatus(content);

        return dao.sendNewMsg();
    }
}
