package org.qii.weiciyuan.support.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ItemBean;
import org.qii.weiciyuan.bean.ListBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: qii
 * Date: 13-4-20
 */
public class TopTipBar extends TextView {

    private Set<String> ids = new HashSet<String>();
    private boolean disappear = false;
    private Runnable lastRunnable;


    public TopTipBar(Context context) {
        this(context, null);
    }

    public TopTipBar(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public TopTipBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setValue(ListBean<?, ?> listData, boolean disappear) {
        this.disappear = disappear;
        ids.clear();
        List<? extends ItemBean> values = listData.getItemList();
        for (ItemBean b : values) {
            if (b != null)
                ids.add(b.getId());
        }
        setCount();
        if (disappear) {
            disappear(3000);
        }
    }

    private void disappear(int duration) {
        if (lastRunnable != null) {
            getHandler().removeCallbacks(lastRunnable);
        }
        lastRunnable = new Runnable() {
            @Override
            public void run() {
                animate().alpha(0).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        setVisibility(View.INVISIBLE);
                        animate().alpha(1.0f).setListener(null);
                    }
                });
            }

        };
        getHandler().postDelayed(lastRunnable, duration);
    }


    private void setCount() {
        int count = ids.size();
        if (count > 0) {
            setVisibility(View.VISIBLE);
            setText(String.format(getContext().getString(R.string.new_messages_count), String.valueOf(ids.size())));
            setBackgroundResource(R.color.top_tip_bar_tip);
        } else {
            disappear(0);
        }
    }

    public void handle(String id) {
        if (disappear) {
            return;
        }
        boolean has = ids.contains(id);
        if (has) {
            ids.remove(id);
            setCount();
        }
    }

    public void clearAndReset() {
        if (disappear) {
            return;
        }
        ids.clear();
        disappear(0);
    }

    public void setError(String error) {
        this.disappear = true;
        setVisibility(View.VISIBLE);
        animate().alpha(1.0f);
        setText(error);
        disappear(5000);
        setBackgroundResource(R.color.top_tip_bar_error);
    }

//    @Override
//    public Parcelable onSaveInstanceState() {
//        Parcelable parcelable= super.onSaveInstanceState();
//        parcelable.
//    }
//
//    @Override
//    public void onRestoreInstanceState(Parcelable state) {
//        super.onRestoreInstanceState(state);
//    }
}
