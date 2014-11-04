package org.qii.weiciyuan.ui.send;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.othercomponent.sendweiboservice.SendRepostService;
import org.qii.weiciyuan.support.database.DraftDBManager;
import org.qii.weiciyuan.support.database.draftbean.RepostDraftBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.search.AtUserActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class WriteRepostActivity extends AbstractWriteActivity<MessageBean> {

    public static final String ACTION_DRAFT = "org.qii.weiciyuan.DRAFT";
    public static final String ACTION_SEND_FAILED = "org.qii.weiciyuan.SEND_FAILED";

    private String token;
    private MessageBean msg;
    private RepostDraftBean repostDraftBean;

    private MenuItem menuEnableComment;
    private MenuItem menuEnableOriComment;

    private boolean savedEnableComment;
    private boolean savedEnableOriComment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(getString(R.string.repost));
        getActionBar().setSubtitle(GlobalContext.getInstance().getCurrentAccountName());

        if (savedInstanceState == null) {

            Intent intent = getIntent();
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(WriteRepostActivity.ACTION_DRAFT)) {
                    handleDraftOperation(intent);
                } else if (action.equals(WriteRepostActivity.ACTION_SEND_FAILED)) {
                    handleFailedOperation(intent);
                }
            } else {
                handleNormalOperation(intent);
            }
        }
    }

    private void handleDraftOperation(Intent intent) {
        AccountBean account = (AccountBean) intent.getParcelableExtra("account");
        token = account.getAccess_token();

        repostDraftBean = (RepostDraftBean) intent.getParcelableExtra("draft");
        getEditTextView().setText(repostDraftBean.getContent());
        msg = repostDraftBean.getMessageBean();

        if (msg.getRetweeted_status() != null) {
            getEditTextView().setHint(
                    "//@" + msg.getRetweeted_status().getUser().getScreen_name() + "：" + msg
                            .getRetweeted_status().getText());
        } else {
            getEditTextView().setHint("@" + msg.getUser().getScreen_name() + "：" + msg.getText());
        }
    }

    public static Intent startBecauseSendFailed(Context context,
            AccountBean accountBean,
            String content,
            MessageBean oriMsg,
            RepostDraftBean repostDraftBean,
            String failedReason) {
        Intent intent = new Intent(context, WriteRepostActivity.class);
        intent.setAction(WriteRepostActivity.ACTION_SEND_FAILED);
        intent.putExtra("account", accountBean);
        intent.putExtra("content", content);
        intent.putExtra("oriMsg", oriMsg);
        intent.putExtra("failedReason", failedReason);
        intent.putExtra("repostDraftBean", repostDraftBean);
        return intent;
    }

    private void handleFailedOperation(Intent intent) {
        token = ((AccountBean) intent.getParcelableExtra("account")).getAccess_token();
        msg = (MessageBean) intent.getParcelableExtra("oriMsg");
        getEditTextView().setText(intent.getStringExtra("content"));

        if (msg.getRetweeted_status() != null) {
            getEditTextView().setHint(
                    "//@" + msg.getRetweeted_status().getUser().getScreen_name() + "：" + msg
                            .getRetweeted_status().getText());
        } else {
            getEditTextView().setHint("@" + msg.getUser().getScreen_name() + "：" + msg.getText());
        }
        getEditTextView().setError(intent.getStringExtra("failedReason"));
        repostDraftBean = (RepostDraftBean) intent.getParcelableExtra("repostDraftBean");
    }

    private void handleNormalOperation(Intent intent) {
        token = intent.getStringExtra("token");
        if (TextUtils.isEmpty(token)) {
            token = GlobalContext.getInstance().getSpecialToken();
        }

        msg = (MessageBean) intent.getParcelableExtra("msg");

        if (msg.getRetweeted_status() != null) {
            getEditTextView()
                    .setText("//@" + msg.getUser().getScreen_name() + ": " + msg.getText());
            getEditTextView().setHint(
                    "//@" + msg.getRetweeted_status().getUser().getScreen_name() + "：" + msg
                            .getRetweeted_status().getText());
        } else {
            getEditTextView().setHint("@" + msg.getUser().getScreen_name() + "：" + msg.getText());
        }
        getEditTextView().setSelection(0);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            savedEnableComment = savedInstanceState.getBoolean("comment");
            savedEnableOriComment = savedInstanceState.getBoolean("oriComment");

            token = savedInstanceState.getString("token");
            msg = (MessageBean) savedInstanceState.getParcelable("msg");
            repostDraftBean = (RepostDraftBean) savedInstanceState.getParcelable("repostDraftBean");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("comment", menuEnableComment.isChecked());
        outState.putBoolean("oriComment", menuEnableOriComment.isChecked());

        outState.putString("token", token);
        outState.putParcelable("msg", msg);
        outState.putParcelable("repostDraftBean", repostDraftBean);
    }

    @Override
    protected boolean canShowSaveDraftDialog() {
        if (repostDraftBean == null) {
            return true;
        } else if (!repostDraftBean.getContent().equals(getEditTextView().getText().toString())) {
            return true;
        }
        return false;
    }

    @Override
    protected void insertTopic() {
        String ori = getEditTextView().getText().toString();
        int index = getEditTextView().getSelectionStart();
        StringBuilder stringBuilder = new StringBuilder(ori);
        stringBuilder.insert(index, "##");
        getEditTextView().setText(stringBuilder.toString());
        getEditTextView().setSelection(index + "##".length() - 1);
    }

    @Override
    public void saveToDraft() {
        if (!TextUtils.isEmpty(getEditTextView().getText().toString())) {
            DraftDBManager.getInstance().insertRepost(getEditTextView().getText().toString(), msg,
                    GlobalContext.getInstance().getCurrentAccountId());
        }
        finish();
    }

    @Override
    protected void removeDraft() {
        if (repostDraftBean != null) {
            DraftDBManager.getInstance().remove(repostDraftBean.getId());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_repostnewactivity, menu);
        menuEnableComment = menu.findItem(R.id.menu_enable_comment);
        menuEnableOriComment = menu.findItem(R.id.menu_enable_ori_comment);

        menuEnableComment.setChecked(savedEnableComment);
        menuEnableOriComment.setChecked(savedEnableOriComment);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (msg.getRetweeted_status() != null) {
            menuEnableOriComment.setVisible(true);
        }

        String contentStr = getEditTextView().getText().toString();
        if (Utility.countWord(contentStr, "//@", 0) > 2) {
            menu.findItem(R.id.menu_short_right).setVisible(true);
            menu.findItem(R.id.menu_short_middle).setVisible(true);
        } else {
            menu.findItem(R.id.menu_short_right).setVisible(false);
            menu.findItem(R.id.menu_short_middle).setVisible(false);
        }

        if (!TextUtils.isEmpty(contentStr)) {
            menu.findItem(R.id.menu_clear).setVisible(true);
        } else {
            menu.findItem(R.id.menu_clear).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
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
            case R.id.menu_short_right:
                shortContent();
                break;
            case R.id.menu_short_middle:
                shortMiddleContent();
                break;
        }
        return true;
    }

    private void shortContent() {
        String content = getEditTextView().getText().toString();
        int index = getEditTextView().getSelectionStart();
        int last = content.lastIndexOf("//@");
        if (last >= 0) {
            String result = content.substring(0, last);
            getEditTextView().setText(result);
            if (index <= result.length()) {
                getEditTextView().setSelection(index);
            }
        }
    }

    private void shortMiddleContent() {
        String content = getEditTextView().getText().toString();
        int index = getEditTextView().getSelectionStart();
        int a = content.lastIndexOf("//@");
        if (a >= 0) {
            String result = content.substring(0, a);
            int b = result.lastIndexOf("//@");
            if (b >= 0) {
                String startPart = content.substring(0, b);
                String endPart = content.substring(a);
                getEditTextView().setText(startPart + endPart);
                if (index <= result.length()) {
                    getEditTextView().setSelection(index);
                }
            }
        }
    }

    @Override
    protected void send() {
        if (canSend()) {
            boolean comment = menuEnableComment.isChecked();
            boolean oriComment = (menuEnableOriComment != null && menuEnableOriComment.isChecked());
            String is_comment = "";
            if (comment && oriComment) {
                is_comment = RepostNewMsgDao.ENABLE_COMMENT_ALL;
            } else if (comment) {
                is_comment = RepostNewMsgDao.ENABLE_COMMENT;
            } else if (oriComment) {
                is_comment = RepostNewMsgDao.ENABLE_ORI_COMMENT;
            }

            Intent intent = new Intent(WriteRepostActivity.this, SendRepostService.class);
            intent.putExtra("oriMsg", msg);
            intent.putExtra("content", getEditTextView().getText().toString());
            intent.putExtra("is_comment", is_comment);
            intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
            intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
            startService(intent);
            finish();
        }
    }

    @Override
    protected boolean canSend() {
        boolean haveToken = !TextUtils.isEmpty(token);
        int sum = Utility.length(getEditTextView().getText().toString());
        int num = 140 - sum;

        boolean contentNumBelow140 = (num >= 0);

        if (haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveToken) {
                Toast.makeText(this, getString(R.string.dont_have_account), Toast.LENGTH_SHORT)
                        .show();
            }

            if (!contentNumBelow140) {
                getEditTextView().setError(getString(R.string.content_words_number_too_many));
            }
        }
        return false;
    }
}
