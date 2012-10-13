package org.qii.weiciyuan.ui.maintimeline;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.userinfo.MyInfoActivity;
import org.qii.weiciyuan.ui.userinfo.StatusesByIdTimeLineFragment;

/**
 * User: qii
 * Date: 12-9-22
 */
public class MyStatussTimeLineFragment extends StatusesByIdTimeLineFragment {

    public MyStatussTimeLineFragment() {

    }


    public MyStatussTimeLineFragment(UserBean userBean, String token) {
        super(userBean, token);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.actionbar_menu_mystatustimelinefragment, menu);
        menu.findItem(R.id.name).setTitle(getString(R.string.personal_info));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.name:
                Intent intent = new Intent(getActivity(), MyInfoActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("user", userBean);
                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
                startActivity(intent);
                break;
        }
        return true;
    }

}
