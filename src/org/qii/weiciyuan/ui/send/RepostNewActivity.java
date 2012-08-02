package org.qii.weiciyuan.ui.send;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import org.qii.weiciyuan.R;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 * Time: 下午4:00
 */
public class RepostNewActivity extends AbstractSendActivity {

    private String rePostContent;

    private EditText content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        rePostContent = intent.getStringExtra("repost_content");

        content= ((EditText) findViewById(R.id.status_new_content));
        content.setText(rePostContent);
    }

    @Override
    protected void executeTask(String content) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
