package org.qii.weiciyuan.ui.browser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.interfaces.IRemoveItem;

/**
 * User: qii
 * Date: 12-11-28
 */
public class RemoveWeiboMsgDialog extends DialogFragment {

    public static interface IRemove {
        public void removeMsg(String id);
    }


    private String id;

    public RemoveWeiboMsgDialog() {

    }

    public RemoveWeiboMsgDialog(String id) {

        this.id = id;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("id", id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            id = savedInstanceState.getString("id");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.askdelete))
                .setMessage(getString(R.string.askdeletemessage))
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IRemove IRemove = (IRemove) getActivity();
                        IRemove.removeMsg(id);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IRemoveItem iRemoveItem = (IRemoveItem) getTargetFragment();
                        iRemoveItem.removeCancel();
                    }
                });

        return builder.create();
    }
}
