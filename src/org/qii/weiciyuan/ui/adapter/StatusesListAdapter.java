package org.qii.weiciyuan.ui.adapter;

import android.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.lib.UpdateString;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.ui.Abstract.ICommander;

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
            buildAvatar(holder.avatar, position, msg.getUser());
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


        if (!TextUtils.isEmpty(msg.getThumbnail_pic()) && GlobalContext.getInstance().isEnablePic()) {
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
