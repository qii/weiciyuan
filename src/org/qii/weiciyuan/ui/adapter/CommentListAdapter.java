package org.qii.weiciyuan.ui.adapter;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.lib.UpdateString;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.ICommander;

import java.util.List;

/**
 * User: qii
 * Date: 12-9-8
 */
public class CommentListAdapter extends AbstractAppListAdapter<CommentBean> {

    private Drawable replyPic = null;
    private Drawable commentPic = null;


    public CommentListAdapter(Fragment fragment, ICommander commander, List<CommentBean> bean, ListView listView, boolean showOriStatus) {
        super(fragment, commander, bean, listView, showOriStatus);

        int[] attrs = new int[]{R.attr.timeline_reply_flag};
        TypedArray ta = fragment.getActivity().obtainStyledAttributes(attrs);
        replyPic = ta.getDrawable(0);

        attrs = new int[]{R.attr.timeline_comment_flag};
        ta = fragment.getActivity().obtainStyledAttributes(attrs);
        commentPic = ta.getDrawable(0);
    }

    @Override
    protected void bindViewData(ViewHolder holder, int position) {

        holder.listview_root.setBackgroundColor(defaultBG);

        if (listView.getCheckedItemPosition() == position + 1)
            holder.listview_root.setBackgroundColor(checkedBG);

        final CommentBean comment = getList().get(position);
        holder.username.setText(comment.getUser().getScreen_name());

        if (comment.getUser() != null) {
            holder.username.setVisibility(View.VISIBLE);
            holder.username.setText(comment.getUser().getScreen_name());
            buildAvatar(holder.avatar, position, comment.getUser());
        } else {
            holder.username.setVisibility(View.INVISIBLE);
            holder.avatar.setVisibility(View.INVISIBLE);
        }


        holder.content.setTextSize(GlobalContext.getInstance().getFontSize());
        holder.content.setText(comment.getListViewSpannableString());

        String time = comment.getListviewItemShowTime();
        UpdateString updateString = new UpdateString(time, holder.time, comment, getActivity());
        if (!holder.time.getText().toString().equals(time)) {
            holder.time.setText(updateString);
        }
        holder.time.setTag(comment.getId());

        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);


        CommentBean reply = comment.getReply_comment();

        if (reply != null&& showOriStatus) {
            holder.repost_layout.setVisibility(View.VISIBLE);
            holder.repost_flag.setVisibility(View.VISIBLE);
            holder.repost_content.setVisibility(View.VISIBLE);
            holder.repost_flag.setImageDrawable(replyPic);
            holder.repost_content.setTextSize(GlobalContext.getInstance().getFontSize());
            holder.repost_content.setText(reply.getListViewReplySpannableString());
        } else {

            MessageBean repost_msg = comment.getStatus();

            if (repost_msg != null && showOriStatus) {
                holder.repost_flag.setImageDrawable(commentPic);
                buildRepostContent(repost_msg, holder, position);
            } else {
                holder.repost_layout.setVisibility(View.GONE);
                holder.repost_flag.setVisibility(View.GONE);
            }

        }
    }

}


