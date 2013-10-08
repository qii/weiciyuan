package org.qii.weiciyuan.support.lib;

import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.ThemeUtility;

/**
 * User: qii
 * Date: 13-10-7
 */
public class ClickableTextViewMentionOnTouchListener implements View.OnTouchListener {

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Layout layout = ((TextView) v).getLayout();

        int x = (int) event.getX();
        int y = (int) event.getY();
        int offset = 0;
        if (layout != null) {

            int line = layout.getLineForVertical(y);
            offset = layout.getOffsetForHorizontal(line, x);
        }

        TextView tv = (TextView) v;
        SpannableString value = SpannableString.valueOf(tv.getText());

        LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                MyURLSpan[] urlSpans = value.getSpans(0, value.length(), MyURLSpan.class);
                boolean find = false;
                int findStart = 0;
                int findEnd = 0;
                for (MyURLSpan urlSpan : urlSpans) {
                    int start = value.getSpanStart(urlSpan);
                    int end = value.getSpanEnd(urlSpan);
                    if (start <= offset && offset <= end) {
                        find = true;
                        findStart = start;
                        findEnd = end;

                        break;
                    }
                }

                if (find) {
                    BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(ThemeUtility.getColor(R.attr.link_pressed_background_color));
                    value.setSpan(backgroundColorSpan, findStart, findEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                return find;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                LongClickableLinkMovementMethod.getInstance().removeLongClickCallback();
                BackgroundColorSpan[] backgroundColorSpans = value.getSpans(0, value.length(), BackgroundColorSpan.class);
                for (BackgroundColorSpan backgroundColorSpan : backgroundColorSpans) {
                    value.removeSpan(backgroundColorSpan);
                }
                break;
        }

        return false;

    }

}
