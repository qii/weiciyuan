package org.qii.weiciyuan.ui.send;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.search.AtUserActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class WriteRepostActivity extends AbstractWriteActivity<MessageBean> {

    private String id;

    private String token;

    private MessageBean msg;

    private MenuItem menuEnableComment;
    private MenuItem menuEnableOriComment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        token = getIntent().getStringExtra("token");
        id = getIntent().getStringExtra("id");
        msg = (MessageBean) getIntent().getSerializableExtra("msg");
        getActionBar().setTitle(getString(R.string.repost));
        getActionBar().setSubtitle(GlobalContext.getInstance().getCurrentAccountName());

        if (msg.getRetweeted_status() != null) {
            getEditTextView().setText("//@" + msg.getUser().getScreen_name() + ": " + msg.getText());
        } else {
            getEditTextView().setHint(getString(R.string.repost) + "//@" + msg.getUser().getScreen_name() + "：" + msg.getText());
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_repostnewactivity, menu);
        menuEnableComment = menu.findItem(R.id.menu_enable_comment);
        menuEnableOriComment = menu.findItem(R.id.menu_enable_ori_comment);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (msg.getRetweeted_status() != null) {
            menuEnableOriComment.setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
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
            case R.id.menu_enable_comment:
                if (menuEnableComment.isChecked()) {
                    menuEnableComment.setChecked(false);
                } else {
                    menuEnableComment.setChecked(true);
                }
                break;
            case R.id.menu_enable_ori_comment:
                if (menuEnableOriComment != null && menuEnableOriComment.isChecked()) {
                    menuEnableOriComment.setChecked(false);
                } else if (menuEnableOriComment != null && !menuEnableOriComment.isChecked()) {
                    menuEnableOriComment.setChecked(true);
                }
                break;
            case R.id.menu_at:
                Intent intent = new Intent(WriteRepostActivity.this, AtUserActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, AT_USER);
                break;
            case R.id.menu_clear:
                clearContentMenu();
                break;
        }
        return true;
    }

    @Override
    protected MessageBean sendData() throws WeiboException {
        String content = getEditTextView().getText().toString();
        if (TextUtils.isEmpty(content)) {
            content = getString(R.string.repost);
        }

        RepostNewMsgDao dao = new RepostNewMsgDao(token, id);

        boolean comment = menuEnableComment.isChecked();
        boolean oriComment = (menuEnableOriComment != null && menuEnableOriComment.isChecked());

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

    @Override
    protected boolean canSend() {

        boolean haveToken = !TextUtils.isEmpty(token);
        boolean contentNumBelow140 = (getEditTextView().getText().toString().length() < 140);

        if (haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveToken) {
                Toast.makeText(this, getString(R.string.dont_have_account), Toast.LENGTH_SHORT).show();
            }

            if (!contentNumBelow140) {
                getEditTextView().setError(getString(R.string.content_words_number_too_many));
            }

        }

        return false;
    }


}
