package org.qii.weiciyuan.ui.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.WeiboAccountBean;
import org.qii.weiciyuan.bean.WeiboUserBean;
import org.qii.weiciyuan.ui.login.AccountActivity;
import org.qii.weiciyuan.ui.preference.SettingActivity;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-30
 * Time: 下午10:13
 * To change this template use File | Settings | File Templates.
 */
public class MyInfoTimeLineFragment extends Fragment {

    private WeiboAccountBean accountBean;

    private WeiboUserBean bean;

    public void setAccountBean(WeiboAccountBean accountBean) {
        this.accountBean = accountBean;
    }

    public void setBean(WeiboUserBean bean) {
        this.bean = bean;
    }

    public MyInfoTimeLineFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_layout, container, false);

        ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
        TextView username = (TextView) view.findViewById(R.id.username);
        TextView jshao = (TextView) view.findViewById(R.id.textView_info);
        Button weibo_number = (Button) view.findViewById(R.id.weibo_number);
        Button following_number = (Button) view.findViewById(R.id.following_number);
        Button fans_number = (Button) view.findViewById(R.id.fans_number);

        username.setText(accountBean.getUsernick());

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.myinfofragment_menu, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_account_management:
                Intent intent = new Intent(getActivity(), AccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.menu_settings:
                  startActivity(new Intent(getActivity(), SettingActivity.class));
                break;

        }
        return true;
    }
}
