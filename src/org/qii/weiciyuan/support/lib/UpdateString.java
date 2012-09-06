package org.qii.weiciyuan.support.lib;

import android.app.Activity;
import android.os.SystemClock;
import android.widget.TextView;
import org.qii.weiciyuan.bean.MessageBean;

/**
 * User: qii
 * Date: 12-9-6
 */
public class UpdateString implements CharSequence {
    private String s;
    private TextView time;
    private MessageBean bean;
    private Activity activity;
    private Thread update;

    public UpdateString(String s, TextView time, MessageBean bean, Activity activity) {
        this.s = s;
        this.time = time;
        this.bean = bean;
        this.activity = activity;
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
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (time.getTag().equals(bean.getText())) {
                                String newValue = bean.getListviewItemShowTime();
                                if (!newValue.equals(time.getText().toString()))
                                    time.setText(newValue);
                            } else {
                                Thread.currentThread().interrupt();
                                update = null;
                            }
                        }
                    });
                }
            }
        });
        update.start();
        return s.toString();
    }
}
