package org.qii.weiciyuan.ui.maintimeline;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.userinfo.StatusesByIdTimeLineFragment;

/**
 * User: qii
 * Date: 12-9-22
 */
public class MyStatussTimeLineFragment extends StatusesByIdTimeLineFragment {

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.mystatustimelinefragment_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.statusesbyidtimelinefragment_status_refresh:

                pullToRefreshListView.startRefreshNow();
                refresh();

                break;
        }
        return true;
    }

}
