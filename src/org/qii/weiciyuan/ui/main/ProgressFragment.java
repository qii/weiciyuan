package org.qii.weiciyuan.ui.main;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-8-5
 * Time: 下午8:52
 * To change this template use File | Settings | File Templates.
 */
public class ProgressFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("刷新中");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);

            return dialog;
        }
    }
