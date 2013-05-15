package org.qii.weiciyuan.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * User: qii
 * Date: 13-5-15
 */
public class DummyLoader extends AsyncTaskLoader {
    public DummyLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Object loadInBackground() {
        return null;
    }
}
