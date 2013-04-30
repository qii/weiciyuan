package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 13-4-29
 */
public class ListViewMiddleMsgLoadingView extends FrameLayout {

    private TextView word;
    private ProgressBar progressBar;

    public ListViewMiddleMsgLoadingView(Context context) {
        super(context);
    }

    public ListViewMiddleMsgLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListViewMiddleMsgLoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.widget_listviewmiddlemsgloadingview, this, true);
        word = (TextView) v.findViewById(R.id.tv_load_middle_msg);
        progressBar = (ProgressBar) v.findViewById(R.id.pb_loading_middle_msg);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void load() {
        word.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void complete() {
        word.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public boolean isLoading() {
        return progressBar.getVisibility() == View.VISIBLE;
    }

    public void setErrorMessage(String errorMessage) {
        complete();
        this.word.setText(errorMessage);
        this.word.setTextColor(getContext().getResources().getColor(R.color.red));

    }
}


