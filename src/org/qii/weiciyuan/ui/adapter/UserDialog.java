package org.qii.weiciyuan.ui.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.database.FilterDBTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;
import org.qii.weiciyuan.ui.userinfo.ManageGroupDialog;

/**
 * User: qii
 * Date: 13-3-10
 */
public class UserDialog extends DialogFragment {


    private UserBean user;

    public UserDialog() {

    }

    public UserDialog(UserBean user) {
        this.user = user;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("user", user);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            user = (UserBean) savedInstanceState.getSerializable("user");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] items = {"@他", "设置分组", "过滤", "取消关注"};
        builder.setTitle(user.getScreen_name())
                .setItems(items, new FriendOnClicker());


        return builder.create();
    }

    private class FriendOnClicker implements DialogInterface.OnClickListener {

        CharSequence[] items = {"@他", "设置分组", "过滤", "取消关注"};

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    Intent intent = new Intent(getActivity(), WriteWeiboActivity.class);
                    intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                    intent.putExtra("content", "@" + user.getScreen_name());
                    intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
                    startActivity(intent);
                    break;
                case 1:
                    ManageGroupDialog manageGroupDialog = new ManageGroupDialog(GlobalContext.getInstance().getGroup(), user.getId());
                    manageGroupDialog.show(getFragmentManager(), "");
                    break;
                case 2:
                    FilterDBTask.addFilterKeyword(user.getScreen_name());
                    Toast.makeText(getActivity(), getString(R.string.filter_successfully), Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    break;
            }
        }
    }
}
