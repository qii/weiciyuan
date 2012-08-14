package org.qii.weiciyuan.ui.timeline;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.OAuthDao;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.browser.SimpleBitmapWorkerTask;
import org.qii.weiciyuan.ui.login.AccountActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.preference.SettingActivity;

/**
 * User: qii
 * Date: 12-7-30
 * Time: 下午10:13
 */
public class MyInfoTimeLineFragment extends Fragment {

    private UserBean bean;

    private ImageView avatar;
    private TextView username;
    private TextView info;
    private Button weibo_number;
    private Button following_number;
    private Button fans_number;

    protected Commander commander;


    public static interface IUserInfo {
        public UserBean getUser();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bean = ((IUserInfo) getActivity()).getUser();
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        setValue();
        new SimpleTask().execute();
    }

    private void setValue() {
        username.setText(bean.getScreen_name());
        info.setText(bean.getDescription());

        String avatarUrl = bean.getAvatar_large();
        if (!TextUtils.isEmpty(avatarUrl)) {
            new SimpleBitmapWorkerTask(avatar).execute(avatarUrl);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info_layout, container, false);
        avatar = (ImageView) view.findViewById(R.id.avatar);
        username = (TextView) view.findViewById(R.id.username);
        info = (TextView) view.findViewById(R.id.textView_info);
        weibo_number = (Button) view.findViewById(R.id.weibo_number);
        following_number = (Button) view.findViewById(R.id.following_number);
        fans_number = (Button) view.findViewById(R.id.fans_number);

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.myinfofragment_menu, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
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

    private class SimpleTask extends AsyncTask<Object, UserBean, UserBean> {

        @Override
        protected UserBean doInBackground(Object... params) {
            UserBean user = new OAuthDao(((MainTimeLineActivity) getActivity()).getToken()).getOAuthUserInfo();
            if (user != null) {
                bean = user;
            }
            return user;
        }

        @Override
        protected void onPostExecute(UserBean o) {

            setValue();
            super.onPostExecute(o);
        }
    }
}
