package org.qii.weiciyuan.ui.browser;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;


/**
 * User: Jiang Qi
 * Date: 12-8-2
 * Time: 下午4:15
 */
public class BrowserCommentListActivity extends AbstractAppActivity {

    private String token = "";
    private String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browsercommentlistactivity_layout);
        token = getIntent().getStringExtra("token");
        id = getIntent().getStringExtra("id");

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CommentsByIdTimeLineFragment fragment = new CommentsByIdTimeLineFragment(token,id);
        fragmentTransaction.add(R.id.listViewFragment, fragment);
        fragmentTransaction.commit();


    }


}
