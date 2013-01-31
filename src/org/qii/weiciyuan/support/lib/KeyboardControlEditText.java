package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * User: qii
 * Date: 13-1-31
 */
public class KeyboardControlEditText extends EditText {
    private boolean mShowKeyboard = true;

    public void setShowKeyboard(boolean value) {
        mShowKeyboard = value;
    }

    public KeyboardControlEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return mShowKeyboard;
    }
}