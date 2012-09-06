package org.qii.weiciyuan.support.lib;

import android.app.Activity;
import android.os.SystemClock;
import android.widget.TextView;
import org.qii.weiciyuan.bean.ItemBean;

import java.lang.ref.WeakReference;

/**
 * User: qii
 * Date: 12-9-6
 */
public class UpdateString implements CharSequence {
    private String s;
    private final WeakReference<TextView> time;
    private ItemBean bean;
    private final WeakReference<Activity> activity;
    private Thread update;

    public UpdateString(String s, TextView time, ItemBean bean, Activity activity) {
        this.s = s;
        this.time = new WeakReference<TextView>(time);
        this.bean = bean;
        this.activity = new WeakReference<Activity>(activity);
    }


    @Override
    public int length() {
        return s.length();
    }

    @Override
    public char charAt(int index) {
        return s.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return s.subSequence(start, end);
    }


    @Override
    public String toString() {
        update = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    SystemClock.sleep(500);
                    if (time != null && time.get() != null && activity != null && activity.get() != null) {
                        Activity mActivity = activity.get();
                        final TextView mTextView = time.get();
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mTextView.getTag().equals(bean.getId())) {
                                    String newValue = bean.getListviewItemShowTime();
                                    if (!newValue.equals(mTextView.getText().toString()))
                                        mTextView.setText(newValue);
                                } else {
                                    Thread.currentThread().interrupt();
                                    update = null;
                                }
                            }
                        });
                    } else {
                        Thread.currentThread().interrupt();
                        update = null;
                    }
                }
            }
        });
        update.start();
        return s.toString();
    }
}
