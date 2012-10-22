package org.qii.weiciyuan.ui.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.EditText;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.database.DatabaseManager;

/**
 * User: qii
 * Date: 12-10-22
 */
public class ModifyFilterDialog extends DialogFragment {

    private String word;

    public ModifyFilterDialog() {

    }

    public ModifyFilterDialog(String word) {
        this.word = word;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText et = new EditText(getActivity());
        et.setText(word);
        et.setSelection(et.getText().toString().length());
        builder.setView(et)
                .setTitle(getString(R.string.modify_filter_word))
                .setPositiveButton(getString(R.string.modify), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newValue = et.getText().toString().trim();
                        if (!TextUtils.isEmpty(word)) {
                            DatabaseManager.getInstance().removeAndGetNewFilterList(word);
                            FilterFragment filterFragment = (FilterFragment) getTargetFragment();
                            filterFragment.addFilter(newValue);
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

