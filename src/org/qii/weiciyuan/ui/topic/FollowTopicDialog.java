package org.qii.weiciyuan.ui.topic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.EditText;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 12-11-26
 */
public class FollowTopicDialog extends DialogFragment {


    public FollowTopicDialog() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText et = new EditText(getActivity());
        et.setHint(getString(R.string.add_topic_hint));
        builder.setView(et)
                .setTitle(getString(R.string.add_topic))
                .setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String word = et.getText().toString().trim();
                        if (!TextUtils.isEmpty(word)) {
                            UserTopicListFragment userTopicListFragment = (UserTopicListFragment) getTargetFragment();
                            userTopicListFragment.addTopic(word);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }
}
