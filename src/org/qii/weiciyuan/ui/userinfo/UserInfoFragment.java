package org.qii.weiciyuan.ui.userinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.show.ShowUserDao;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.browser.SimpleBitmapWorkerTask;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 * Time: 下午3:19
 */
public class UserInfoFragment extends android.app.Fragment {

    private UserBean bean;

    private ImageView avatar;
    private TextView username;
    private TextView info;
    private Button weibo_number;
    private Button following_number;
    private Button fans_number;
    private Button fav_number;

    protected ICommander commander;


    public UserInfoFragment() {
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

    }

    @Override
    public void onStart() {
        super.onStart();
        // new SimpleTask().execute();
    }

    private void setValue() {
        username.setText(bean.getScreen_name());
        info.setText(bean.getDescription());

        String avatarUrl = bean.getAvatar_large();
        if (!TextUtils.isEmpty(avatarUrl)) {
            new SimpleBitmapWorkerTask(avatar).execute(avatarUrl);
        }

        setTextViewNum(weibo_number, bean.getStatuses_count());
        setTextViewNum(fans_number, bean.getFollowers_count());
        setTextViewNum(following_number, bean.getFriends_count());
        setTextViewNum(fav_number, bean.getFavourites_count());

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
        fav_number = (Button) view.findViewById(R.id.fav_number);

        weibo_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserInfoStatusesActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", bean);
                startActivity(intent);
            }
        });

        following_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendListActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", bean);
                startActivity(intent);
            }
        });
        fans_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FanListActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", bean);
                startActivity(intent);
            }
        });
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.infofragment_menu, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                new SimpleTask().execute();
                break;
            case R.id.menu_edit:
                Toast.makeText(getActivity(), "接口没开放", Toast.LENGTH_SHORT).show();
                break;

        }
        return true;
    }

    private class SimpleTask extends AsyncTask<Object, UserBean, UserBean> {

        @Override
        protected UserBean doInBackground(Object... params) {
            UserBean user = new ShowUserDao(((IToken) getActivity()).getToken())
                    .setUid(bean.getId()).getUserInfo();
            if (user != null) {
                bean = user;
            } else {
                cancel(true);
            }
            return user;
        }

        @Override
        protected void onPostExecute(UserBean o) {

            setValue();
            super.onPostExecute(o);
        }
    }

    private void setTextViewNum(TextView tv, String num) {

        String name = tv.getText().toString();
        String value = "(" + num + ")";
        if (!name.endsWith(")")) {
            tv.setText(name + value);
        } else {
            int index = name.indexOf("(");
            String newName = name.substring(0, index);
            tv.setText(newName + value);
        }

    }
}
