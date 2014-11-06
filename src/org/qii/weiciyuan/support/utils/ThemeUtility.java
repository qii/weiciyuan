package org.qii.weiciyuan.support.utils;

import org.qii.weiciyuan.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.SearchView;
import android.widget.TextView;

/**
 * User: qii
 * Date: 13-8-4
 */
public class ThemeUtility {

    public static int getColor(int attr) {
        return getColor(GlobalContext.getInstance().getActivity(), attr);
    }

    public static int getColor(Activity activity, int attr) {
        int[] attrs = new int[]{attr};
        TypedArray ta = activity.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, 430);
        ta.recycle();
        return color;
    }

    public static Drawable getDrawable(int attr) {
        int[] attrs = new int[]{attr};
        Context context = GlobalContext.getInstance().getActivity();
        TypedArray ta = context.obtainStyledAttributes(attrs);
        Drawable drawable = ta.getDrawable(0);
        ta.recycle();
        return drawable;
    }

    public static int getResourceId(int attr) {
        int[] attrs = new int[]{attr};
        Context context = GlobalContext.getInstance().getActivity();
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int id = ta.getResourceId(0, 430);
        ta.recycle();
        return id;
    }

    public static int getDimensionPixelSize(Activity activity, int attr, int defaultValue) {
        int[] attrs = new int[]{attr};
        TypedArray ta = activity.obtainStyledAttributes(attrs);
        int value = ta.getDimensionPixelSize(0, defaultValue);
        ta.recycle();
        return value;
    }

    //can't find a public theme attr to modify actionbar searchview text color
    public static void customActionBarSearchViewTextColor(SearchView searchView) {
        int id = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
    }

    //android:actionModeShareDrawalbe is not a public attr
    public static int getActionBarShareItemIcon() {
        return R.drawable.ic_menu_share_holo_dark;
    }
}
