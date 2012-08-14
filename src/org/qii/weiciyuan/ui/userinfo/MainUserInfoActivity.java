package org.qii.weiciyuan.ui.userinfo;

import android.os.Bundle;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 * Time: 下午2:59
 */
public class MainUserInfoActivity extends AbstractAppActivity implements IUserInfo,
        IToken {

    private String token;
    private UserBean bean;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        token = getIntent().getStringExtra("token");
        bean = (UserBean) (getIntent().getSerializableExtra("user"));
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public UserBean getUser() {
        return null;
    }
}
