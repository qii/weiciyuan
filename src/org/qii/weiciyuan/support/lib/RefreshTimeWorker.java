package org.qii.weiciyuan.support.lib;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import org.qii.weiciyuan.bean.ItemBean;
import org.qii.weiciyuan.ui.adapter.AbstractAppListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;

/**
 * User: qii
 * Date: 12-10-1
 */
public class RefreshTimeWorker {

    private AbstractTimeLineFragment fragment;

    public RefreshTimeWorker(AbstractTimeLineFragment fragment) {
        this.fragment = fragment;
    }

    private AbstractTimeLineFragment getCurrentShowFragment() {
        return fragment;
    }

    private FragmentActivity getActivity() {
        return fragment.getActivity();
    }

    public void refreshTime() {

        if (fragment == null)
            return;

        if (getActivity() == null)
            return;

        if (getCurrentShowFragment() == null)
            return;
        if (getCurrentShowFragment().isListViewFling())
            return;


        int start = getCurrentShowFragment().getListView().getFirstVisiblePosition();
        int end = getCurrentShowFragment().getListView().getLastVisiblePosition();


        int visibleItemNum = getCurrentShowFragment().getListView().getChildCount();
        for (int i = 0; i < visibleItemNum; i++) {
            if (start + i > 0 && getCurrentShowFragment().getAdapter() != null) {
                final Object object = getCurrentShowFragment().getAdapter().getItem(start + i - 1);
                if (object instanceof ItemBean) {
                    final int finalI = i;
                    final ItemBean msg = (ItemBean) object;
                    final String timeString = msg.getListviewItemShowTime();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            View view = getCurrentShowFragment().getListView().getChildAt(finalI);
                            //it is so strange that one time view throw null exception....
                            if (view == null)
                                return;
                            AbstractAppListAdapter.ViewHolder holder = (AbstractAppListAdapter.ViewHolder) view.getTag();
                            TextView time = holder.time;
                            if (time != null)
                                time.setText(timeString);
                        }
                    });
                }
            }
        }
    }
}




