package org.qii.weiciyuan.support.lib;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Parcel;
import android.provider.Browser;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.browser.BrowserWebActivity;

/**
 * User: qii
 * Date: 12-8-20
 */
public class MyURLSpan extends ClickableSpan implements ParcelableSpan {

    private final String mURL;

    public MyURLSpan(String url) {
        mURL = url;
    }

    public MyURLSpan(Parcel src) {
        mURL = src.readString();
    }

    public int getSpanTypeId() {
        return 11;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mURL);
    }

    public String getURL() {
        return mURL;
    }

    public void onClick(View widget) {
        Uri uri = Uri.parse(getURL());
        Context context = widget.getContext();
        if (uri.getScheme().startsWith("http")) {
            Intent intent = new Intent(context, BrowserWebActivity.class);
            intent.putExtra("url", uri.toString());
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
            context.startActivity(intent);
        }
    }

    public void onLongClick(View widget) {
        Uri data = Uri.parse(getURL());
        if (data != null) {
            String d = data.toString();
            String newValue = "";
            if (d.startsWith("org.qii.weiciyuan")) {
                int index = d.lastIndexOf("/");
                newValue = d.substring(index + 1);
            } else if (d.startsWith("http")) {
                newValue = d;
            }
            if (!TextUtils.isEmpty(newValue)) {
                ClipboardManager cm = (ClipboardManager) widget.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", newValue));
                Toast.makeText(widget.getContext(), String.format(widget.getContext().getString(R.string.have_copied), newValue), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        int[] attrs = new int[]{R.attr.link_color};
        TypedArray ta = GlobalContext.getInstance().getActivity().obtainStyledAttributes(attrs);
        int drawableFromTheme = ta.getColor(0, 430);
        tp.setColor(drawableFromTheme);
//        tp.setUnderlineText(true);
    }
}
