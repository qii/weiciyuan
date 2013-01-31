package org.qii.weiciyuan.support.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * User: qii
 * Date: 13-1-18
 * from top to bottom:statusbar, actionbar, app content, keyboard
 */
public class SmileyPickerUtility {
    public static void hideSoftInput(EditText paramEditText) {
        ((InputMethodManager) GlobalContext.getInstance().getSystemService("input_method")).hideSoftInputFromWindow(paramEditText.getWindowToken(), 0);
    }

    public static void showKeyBoard(final EditText paramEditText) {
        paramEditText.requestFocus();
        paramEditText.post(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) GlobalContext.getInstance().getSystemService("input_method")).showSoftInput(paramEditText, 0);
            }
        });
    }


    public static int getScreenHeight(Activity paramActivity) {
        Display display = paramActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static int getStatusBarHeight(Activity paramActivity) {
        Rect localRect = new Rect();
        paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        return localRect.top;

    }

    public static int getActionBarHeight(Activity paramActivity) {
        int contentViewTop =
                paramActivity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        AppLogger.e("contentViewTop=" + contentViewTop);

        return contentViewTop - getStatusBarHeight(paramActivity);
    }

    //below status bar,include actionbar, above softkeyboard
    public static int getAppHeight(Activity paramActivity) {
        Rect localRect = new Rect();
        paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        return localRect.height();
    }

    //below actionbar, above softkeyboard
    public static int getAppContentHeight(Activity paramActivity) {
        return SmileyPickerUtility.getAppHeight(paramActivity) - SmileyPickerUtility.getActionBarHeight(paramActivity);
    }

    public static int getKeyboardHeight(Activity paramActivity) {

        return SmileyPickerUtility.getScreenHeight(paramActivity)
                - SmileyPickerUtility.getStatusBarHeight(paramActivity)
                - SmileyPickerUtility.getAppHeight(paramActivity);

    }

    public static boolean isKeyBoardShow(Activity paramActivity) {
        return getKeyboardHeight(paramActivity) == 0;
    }
}
