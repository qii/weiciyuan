package org.qii.weiciyuan.ui.send;

import android.content.Context;
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

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class RepostNewActivity extends AbstractNewActivity<MessageBean> {

    private String id;

    private String token;

    private boolean enableComment = false;
    private String enableCommentString;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        token = getIntent().getStringExtra("token");
        id = getIntent().getStringExtra("id");
        MessageBean msg = (MessageBean) getIntent().getSerializableExtra("msg");
        getActionBar().setTitle(getString(R.string.repost));
        getActionBar().setSubtitle(GlobalContext.getInstance().getCurrentAccountName());

        if (msg.getRetweeted_status() != null) {
            getEditTextView().setText("//@" + msg.getUser().getScreen_name() + ": " + msg.getText());
        } else {
            getEditTextView().setHint(getString(R.string.repost) + "//@" + msg.getUser().getScreen_name() + "：" + msg.getText());
        }
        enableCommentString = getString(R.string.disable_comment_when_repost);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.repostnewactivity_menu, menu);
        menu.findItem(R.id.menu_enable_comment).setTitle(enableCommentString);

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
            case R.id.menu_enable_comment:
                if (enableComment) {
                    enableComment = false;
                    enableCommentString = getString(R.string.disable_comment_when_repost);
                } else {
                    enableCommentString = getString(R.string.enable_comment_when_repost);
                    enableComment = true;
                }
                invalidateOptionsMenu();
                break;
//            case R.id.menu_clear:
//                clearContentMenu();
//                break;
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
        if (enableComment) {
            dao.setIs_comment(true);
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
