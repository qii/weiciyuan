package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * User: qii
 * Date: 13-1-31
 */
public class KeyboardControlEditText extends AutoCompleteTextView {
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