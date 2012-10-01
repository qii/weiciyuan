package org.qii.weiciyuan.ui.adapter;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.lib.UpdateString;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;
import org.qii.weiciyuan.ui.widgets.PictureDialogFragment;

import java.util.List;

/**
 * User: qii
 * Date: 12-8-19
 */
public class StatusesListAdapter extends AbstractAppListAdapter<MessageBean> {


    public StatusesListAdapter(Fragment fragment, ICommander commander, List<MessageBean> bean, ListView listView, boolean showOriStatus) {
        super(fragment, commander, bean, listView, showOriStatus);
    }


    @Override
    protected void bindViewData(ViewHolder holder, int position) {


        holder.listview_root.setBackgroundColor(defaultBG);

        if (listView.getCheckedItemPosition() == position + 1)
            holder.listview_root.setBackgroundColor(checkedBG);

        final MessageBean msg = bean.get(position);

        if (msg.getUser() != null) {
            holder.username.setVisibility(View.VISIBLE);
            holder.username.setText(msg.getUser().getScreen_name());
            String image_url = msg.getUser().getProfile_image_url();
            if (!TextUtils.isEmpty(image_url) && GlobalContext.getInstance().isEnablePic()) {
                holder.avatar.setVisibility(View.VISIBLE);
                //when listview is flying,app dont download avatar and picture
                boolean isFling = ((AbstractTimeLineFragment) fragment).isListViewFling();
                //50px avatar or 180px avatar
                String url;
                if (GlobalContext.getInstance().getEnableBigAvatar()) {
                    url = msg.getUser().getAvatar_large();
                } else {
                    url = msg.getUser().getProfile_image_url();
                }
                commander.downloadAvatar(holder.avatar, url, position, listView, isFling);
            } else {
                holder.avatar.setVisibility(View.GONE);
            }
        } else {
            holder.username.setVisibility(View.INVISIBLE);
            holder.avatar.setVisibility(View.INVISIBLE);
        }
        holder.content.setTextSize(GlobalContext.getInstance().getFontSize());
        if (!TextUtils.isEmpty(msg.getListViewSpannableString())) {
            holder.content.setText(msg.getListViewSpannableString());
        } else {
            ListViewTool.addJustHighLightLinks(msg);
            holder.content.setText(msg.getListViewSpannableString());
        }
        String time = msg.getListviewItemShowTime();
        UpdateString updateString = new UpdateString(time, holder.time, msg, getActivity());
        if (!holder.time.getText().toString().equals(time)) {
            holder.time.setText(updateString);
        }
        holder.time.setTag(msg.getId());

        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);
        holder.content_pic.setVisibility(View.GONE);

        MessageBean repost_msg = msg.getRetweeted_status();

        if (repost_msg != null && showOriStatus) {
            holder.repost_layout.setVisibility(View.VISIBLE);
            holder.repost_flag.setVisibility(View.VISIBLE);
            buildRepostContent(repost_msg, holder, position);
        } else {
            holder.repost_layout.setVisibility(View.GONE);
            holder.repost_flag.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(msg.getThumbnail_pic()) && GlobalContext.getInstance().isEnablePic()) {
            buildPic(msg, holder.content_pic, position);

        }

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", msg.getUser());
                getActivity().startActivity(intent);
            }
        });
    }

    private void buildRepostContent(final MessageBean repost_msg, ViewHolder holder, int position) {
        holder.repost_content.setVisibility(View.VISIBLE);
        holder.repost_content.setTextSize(GlobalContext.getInstance().getFontSize());
        holder.repost_content.setText(repost_msg.getListViewSpannableString());

        if (!TextUtils.isEmpty(repost_msg.getBmiddle_pic()) && GlobalContext.getInstance().isEnablePic()) {
            buildPic(repost_msg, holder.repost_content_pic, position);
        }
        if (repost_msg.getUser() != null && GlobalContext.getInstance().isEnablePic()) {

        } else {
            holder.repost_flag.setVisibility(View.GONE);
        }
    }


    private void buildPic(final MessageBean msg, ImageView view, int position) {
        view.setVisibility(View.VISIBLE);

        String picUrl;

        boolean isFling = ((AbstractTimeLineFragment) fragment).isListViewFling();

        if (GlobalContext.getInstance().getEnableBigPic()) {
            picUrl = msg.getBmiddle_pic();
            commander.downContentPic(view, picUrl, position, listView, FileLocationMethod.picture_bmiddle, isFling);

        } else {
            picUrl = msg.getThumbnail_pic();
            commander.downContentPic(view, picUrl, position, listView, FileLocationMethod.picture_thumbnail, isFling);

        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureDialogFragment progressFragment = new PictureDialogFragment(msg.getBmiddle_pic(), msg.getOriginal_pic());
                progressFragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });
    }


}
