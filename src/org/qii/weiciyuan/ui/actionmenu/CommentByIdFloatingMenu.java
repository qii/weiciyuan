package org.qii.weiciyuan.ui.actionmenu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.browser.BrowserCommentActivity;
import org.qii.weiciyuan.ui.send.WriteReplyToCommentActivity;

/**
 * User: qii
 * Date: 12-12-6
 */
public class CommentByIdFloatingMenu extends DialogFragment {

    private CommentBean bean;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("bean", bean);
    }

    public CommentByIdFloatingMenu() {

    }


    public CommentByIdFloatingMenu(CommentBean bean) {
        this.bean = bean;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            bean = (CommentBean) savedInstanceState.getParcelable("bean");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(this.bean.getUser().getScreen_name());
        String[] str = {getString(R.string.reply_to_comment), getString(R.string.view_comment)};
        builder.setItems(str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case 0:
                        intent = new Intent(getActivity(), WriteReplyToCommentActivity.class);
                        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                        intent.putExtra("msg", bean);
                        getActivity().startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(getActivity(), BrowserCommentActivity.class);
                        intent.putExtra("comment", bean);
                        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                        getActivity().startActivity(intent);
                        break;
                }

            }
        });

        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}

