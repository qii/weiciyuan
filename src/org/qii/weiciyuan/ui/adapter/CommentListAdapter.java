package org.qii.weiciyuan.ui.adapter;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.lib.UpdateString;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;
import org.qii.weiciyuan.ui.widgets.PictureDialogFragment;

import java.util.List;

/**
 * User: qii
 * Date: 12-9-8
 */
public class CommentListAdapter extends AbstractAppListAdapter<CommentBean> {


    public CommentListAdapter(FragmentActivity activity, ICommander commander, List<CommentBean> bean, ListView listView, boolean showOriStatus) {
        super(activity, commander, bean, listView, showOriStatus);
    }


    @Override
    protected void bindViewData(ViewHolder holder, int position) {

        holder.listview_root.setBackgroundColor(defaultBG);

        if (listView.getCheckedItemPosition() == position + 1)
            holder.listview_root.setBackgroundColor(checkedBG);

        final CommentBean msg = getList().get(position);
        MessageBean repost_msg = msg.getStatus();


        holder.username.setText(msg.getUser().getScreen_name());
        String image_url = msg.getUser().getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            commander.downloadAvatar(holder.avatar, msg.getUser().getProfile_image_url(), position, listView);
            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, UserInfoActivity.class);
                    intent.putExtra("token", ((IToken) activity).getToken());
                    intent.putExtra("user", msg.getUser());
                    activity.startActivity(intent);
                }
            });
        }
        holder.content.setTextSize(GlobalContext.getInstance().getFontSize());
        holder.content.setText(msg.getListViewSpannableString());

        String time = msg.getListviewItemShowTime();
        UpdateString updateString = new UpdateString(time, holder.time, msg, activity);
        if (!holder.time.getText().toString().equals(time)) {
            holder.time.setText(updateString);
        }
        holder.time.setTag(msg.getId());

        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);

        if (repost_msg != null && showOriStatus) {
            buildRepostContent(repost_msg, holder, position);
        }


    }

    private void buildRepostContent(final MessageBean repost_msg, ViewHolder holder, int position) {
        holder.repost_content.setVisibility(View.VISIBLE);
        if (repost_msg.getUser() != null) {
            holder.repost_content.setTextSize(GlobalContext.getInstance().getFontSize());
            holder.repost_content.setText(repost_msg.getListViewSpannableString());
        } else {
            holder.repost_content.setText(repost_msg.getText());

        }
        if (!TextUtils.isEmpty(repost_msg.getThumbnail_pic())) {
            holder.repost_content_pic.setVisibility(View.VISIBLE);
            String picUrl;
            if (GlobalContext.getInstance().getEnableBigPic()) {
                picUrl = repost_msg.getBmiddle_pic();
                commander.downContentPic(holder.repost_content_pic, picUrl, position, listView, FileLocationMethod.picture_bmiddle);

            } else {
                picUrl = repost_msg.getThumbnail_pic();
                commander.downContentPic(holder.repost_content_pic, picUrl, position, listView, FileLocationMethod.picture_thumbnail);

            }
            holder.repost_content_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PictureDialogFragment progressFragment = new PictureDialogFragment(repost_msg.getBmiddle_pic(), repost_msg.getOriginal_pic());
                    progressFragment.show(activity.getSupportFragmentManager(), "");
                }
            });
        }
    }


}


