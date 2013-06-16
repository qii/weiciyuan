package org.qii.weiciyuan.ui.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.EditText;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.preference.filter.AbstractFilterFragment;

/**
 * User: qii
 * Date: 12-9-21
 */
public class AddFilterDialog extends DialogFragment {


    public AddFilterDialog() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText et = new EditText(getActivity());
        builder.setView(et)
                .setTitle(getString(R.string.input_filter_word))
                .setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String word = et.getText().toString().trim();
                        if (!TextUtils.isEmpty(word)) {
                            AbstractFilterFragment filterFragment = (AbstractFilterFragment) getTargetFragment();
                            filterFragment.addFilter(word);
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
