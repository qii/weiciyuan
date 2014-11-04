package org.qii.weiciyuan.ui.send;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.ThemeUtility;
import org.qii.weiciyuan.support.utils.Utility;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

/**
 * User: qii
 * Date: 12-9-2
 */
public class TextNumLimitWatcher implements TextWatcher {

    private TextView tv;
    private EditText et;
    private Activity activity;

    public TextNumLimitWatcher(TextView tv, EditText et, Activity activity) {
        this.tv = tv;
        this.et = et;
        this.activity = activity;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int sum = Utility.length(et.getText().toString());

        int left = 140 - sum;

        if (left == 140) {
            tv.setText(activity.getString(R.string.send));
        } else {
            tv.setText(String.valueOf(left));
        }
        if (left < 0) {
            tv.setTextColor(activity.getResources().getColor(R.color.red));
        } else if (left >= 0 && left <= 140) {
            tv.setTextColor(ThemeUtility
                    .getColor(activity, android.R.attr.actionMenuTextColor));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
