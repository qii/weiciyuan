package org.qii.weiciyuan.ui.browser;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.*;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.TimeLineUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.send.WriteReplyToCommentActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

/**
 * User: qii
 * Date: 12-9-19
 */
public class BrowserCommentFragment extends Fragment {

    private CommentBean msg;

    private TextView username;
    private TextView content;
    private TextView time;
    private TextView source;

    private ImageView avatar;


    private ShareActionProvider mShareActionProvider;


    public BrowserCommentFragment() {
    }


    public BrowserCommentFragment(CommentBean msg) {
        this.msg = msg;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("msg", msg);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            msg = (CommentBean) savedInstanceState.getParcelable("msg");
        }
        buildViewData();


    }

    @Override
    public void onDetach() {
        super.onDetach();

        avatar.setImageDrawable(null);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browserweibomsgactivity_layout, container, false);

        username = (TextView) view.findViewById(R.id.username);
        content = (TextView) view.findViewById(R.id.content);
        time = (TextView) view.findViewById(R.id.time);
        source = (TextView) view.findViewById(R.id.source);


        avatar = (ImageView) view.findViewById(R.id.avatar);


        view.findViewById(R.id.first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("user", msg.getUser());
                startActivity(intent);
            }
        });


        return view;
    }

    private void buildViewData() {
        if (msg.getUser() != null) {
            username.setText(msg.getUser().getScreen_name());
            String url = msg.getUser().getProfile_image_url();
            Bitmap bitmap = GlobalContext.getInstance().getAvatarCache().get(url);
            if (bitmap != null) {
                avatar.setImageBitmap(bitmap);
            } else {

//                ProfileAvatarReadWorker avatarTask = new ProfileAvatarReadWorker(avatar, FileLocationMethod.avatar_small);
//                avatarTask.execute(url);
            }

        }
        content.setText(msg.getText());
        TimeLineUtility.addLinks(content);

        time.setText(msg.getListviewItemShowTime());


        if (!TextUtils.isEmpty(msg.getSource())) {
            source.setText(Html.fromHtml(msg.getSource()).toString());
        }

        buildShareActionMenu();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar_menu_browserweibocommentactivity, menu);

        MenuItem item = menu.findItem(R.id.menu_share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        buildShareActionMenu();

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void buildShareActionMenu() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        if (msg != null) {
            sharingIntent.putExtra(Intent.EXTRA_TEXT, msg.getText());
            if (Utility.isIntentSafe(getActivity(), sharingIntent) && mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(sharingIntent);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            case R.id.menu_comment:
                intent = new Intent(getActivity(), WriteReplyToCommentActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("msg", msg);
                getActivity().startActivity(intent);


                break;

            case R.id.menu_share:

                buildShareActionMenu();
                return true;
            case R.id.menu_copy:
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", content.getText().toString()));
                Toast.makeText(getActivity(), getString(R.string.copy_successfully), Toast.LENGTH_SHORT).show();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


}
