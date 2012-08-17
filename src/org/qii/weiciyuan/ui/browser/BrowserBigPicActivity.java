package org.qii.weiciyuan.ui.browser;

import android.os.Bundle;
import android.widget.ImageView;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-8-18
 * Time: 上午12:05
 */
public class BrowserBigPicActivity extends AbstractAppActivity {

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView view=new ImageView(this);
        setContentView(view);

        url = getIntent().getStringExtra("url");

       new SimpleBitmapWorkerTask(view).execute(url);
    }
}
