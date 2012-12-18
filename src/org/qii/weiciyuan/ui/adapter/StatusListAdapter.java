package org.qii.weiciyuan.ui.adapter;

import android.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.ui.interfaces.ICommander;

import java.util.List;

/**
 * User: qii
 * Date: 12-8-19
 */
public class StatusListAdapter extends AbstractAppListAdapter<MessageBean> {


    public StatusListAdapter(Fragment fragment, ICommander commander, List<MessageBean> bean, ListView listView, boolean showOriStatus) {
        super(fragment, commander, bean, listView, showOriStatus);
    }


    @Override
    protected void bindViewData(ViewHolder holder, int position) {

        holder.listview_root.setBackgroundColor(defaultBG);

        if (listView.getCheckedItemPosition() == position + 1)
            holder.listview_root.setBackgroundColor(checkedBG);

        final MessageBean msg = bean.get(position);
        UserBean user = msg.getUser();
        if (user != null) {
            holder.username.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(user.getRemark())) {
                holder.username.setText(new StringBuilder(user.getScreen_name()).append("(").append(user.getRemark()).append(")").toString());
            } else {
                holder.username.setText(user.getScreen_name());
            }
            if (!showOriStatus && !SettingUtility.getEnableCommentRepostListAvatar()) {
                holder.avatar.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
            } else {
                buildAvatar(holder.avatar, position, user);
            }

        } else {
            holder.username.setVisibility(View.INVISIBLE);
            holder.avatar.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(msg.getListViewSpannableString())) {
            holder.content.setText(msg.getListViewSpannableString());
        } else {
            ListViewTool.addJustHighLightLinks(msg);
            holder.content.setText(msg.getListViewSpannableString());
        }

        holder.time.setTime(msg.getMills());

        if (showOriStatus) {
            if (msg.getReposts_count() == 0 && msg.getComments_count() == 0) {
                holder.count_layout.setVisibility(View.GONE);
            } else {
                holder.count_layout.setVisibility(View.VISIBLE);

                if (msg.getReposts_count() > 0) {
                    holder.repost_count.setText(String.valueOf(msg.getReposts_count()));
                    holder.repost_count.setVisibility(View.VISIBLE);
                } else {
                    holder.repost_count.setVisibility(View.GONE);
                }

                if (msg.getComments_count() > 0) {
                    holder.comment_count.setText(String.valueOf(msg.getComments_count()));
                    holder.comment_count.setVisibility(View.VISIBLE);
                } else {
                    holder.comment_count.setVisibility(View.GONE);
                }
            }
        }

        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);
        holder.content_pic.setVisibility(View.GONE);


        if (!TextUtils.isEmpty(msg.getThumbnail_pic())) {
            buildPic(msg, holder.content_pic, position);

        }

        MessageBean repost_msg = msg.getRetweeted_status();

        if (repost_msg != null && showOriStatus) {
            holder.repost_layout.setVisibility(View.VISIBLE);
            holder.repost_flag.setVisibility(View.VISIBLE);
            buildRepostContent(repost_msg, holder, position);
        } else {
            holder.repost_layout.setVisibility(View.GONE);
            holder.repost_flag.setVisibility(View.GONE);
        }
    }


}
