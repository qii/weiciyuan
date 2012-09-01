package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IWeiboMsgInfo;

/**
 * User: Jiang Qi
 * Date: 12-8-1
 */
public class BrowserWeiboMsgActivity extends AbstractAppActivity implements IWeiboMsgInfo, IToken {

    private MessageBean msg;
    private String token;


    private String comment_sum = "";
    private String retweet_sum = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.weibo));

        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        msg = (MessageBean) intent.getSerializableExtra("msg");


        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new BrowserWeiboMsgFragment(msg))
                .commit();

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.browserweibomsgactivity_menu, menu);
//
//        menu.getItem(0).setTitle(menu.getItem(0).getTitle() + "(" + retweet_sum + ")");
//        menu.getItem(1).setTitle(menu.getItem(1).getTitle() + "(" + comment_sum + ")");
//
//        boolean fav = msg.isFavorited();
//        if (fav) {
//            menu.findItem(R.id.menu_fav).setIcon(R.drawable.fav_un_black);
//        } else {
//            menu.findItem(R.id.menu_fav).setIcon(R.drawable.fav_en_black);
//        }
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public MessageBean getMsg() {
        return msg;
    }



}
