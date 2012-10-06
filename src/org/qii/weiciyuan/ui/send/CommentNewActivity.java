package org.qii.weiciyuan.ui.send;

import android.content.Context;
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
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class CommentNewActivity extends AbstractNewActivity<ItemBean> {

    private String id;
    private String token;
    private MenuItem enableCommentOri;
    private MenuItem enableRepost;

    private MessageBean msg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(R.string.comments);
        getActionBar().setSubtitle(GlobalContext.getInstance().getCurrentAccountName());

        token = getIntent().getStringExtra("token");
        id = getIntent().getStringExtra("id");
        msg = (MessageBean) getIntent().getSerializableExtra("msg");

        getActionBar().setTitle(getString(R.string.comments));
        getEditTextView().setHint("@" + msg.getUser().getScreen_name() + "：" + msg.getText());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.commentnewactivity_menu, menu);
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
        boolean contentNumBelow140 = (getEditTextView().getText().toString().length() < 140);

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
        }
        return true;
    }

    @Override
    protected ItemBean sendData() throws WeiboException {
        if (!enableRepost.isChecked()) {
            CommentNewMsgDao dao = new CommentNewMsgDao(token, id, ((EditText) findViewById(R.id.status_new_content)).getText().toString());
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
     *
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

        RepostNewMsgDao dao = new RepostNewMsgDao(token, id);

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
