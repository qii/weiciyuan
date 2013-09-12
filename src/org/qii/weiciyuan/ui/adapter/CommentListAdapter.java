package org.qii.weiciyuan.ui.adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.lib.AutoScrollListView;
import org.qii.weiciyuan.support.lib.TopTipBar;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.send.WriteReplyToCommentActivity;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * User: qii
 * Date: 12-9-8
 */
public class CommentListAdapter extends AbstractAppListAdapter<CommentBean> {


    private Map<ViewHolder, Drawable> bg = new WeakHashMap<ViewHolder, Drawable>();

    private TopTipBar topTipBar;

    private Handler handler = new Handler();

    private AbsListView.OnScrollListener onScrollListener;

    public CommentListAdapter(Fragment fragment, List<CommentBean> bean, ListView listView, boolean showOriStatus) {
        this(fragment, bean, listView, showOriStatus, false);
    }

    public CommentListAdapter(Fragment fragment, List<CommentBean> bean, ListView listView, boolean showOriStatus, boolean pref) {
        super(fragment, bean, listView, showOriStatus, pref);

    }

    public void setTopTipBar(TopTipBar bar) {
        this.topTipBar = bar;
        AutoScrollListView autoScrollListView = (AutoScrollListView) listView;
        autoScrollListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                VelocityListView velocityListView = (VelocityListView) view;
//                if (velocityListView.getVelocity() < 0) {
//                    topTipBar.hideCount();
//                } else if (velocityListView.getVelocity() > 0) {
//                    if (topTipBar.getValues().size() == 0) {
//                        return;
//                    }

                View childView = Utility.getListViewItemViewFromPosition(listView, firstVisibleItem);

                if (childView == null) {
                    return;
                }

                int position = firstVisibleItem - ((ListView) view).getHeaderViewsCount();

                if (childView.getTop() == 0 && position <= 0) {
                    topTipBar.clearAndReset();
                } else {
                    handle(position + 1);
                }
            }
//            }

            private void handle(int position) {
                if (position > 0 && topTipBar != null && position < bean.size()) {
                    CommentBean next = bean.get(position);
                    if (next != null) {
                        CommentBean helperMsg = bean.get(position - 1);
                        long helperId = 0L;
                        if (helperMsg != null) {
                            helperId = helperMsg.getIdLong();
                        }
                        topTipBar.handle(next.getIdLong(), helperId);
                    }
                }

            }
        });
    }

    @Override
    protected void bindViewData(final ViewHolder holder, int position) {

        Drawable drawable = bg.get(holder);
        if (drawable != null) {
            holder.listview_root.setBackgroundDrawable(drawable);

        } else {
            drawable = holder.listview_root.getBackground();
            bg.put(holder, drawable);
        }

        if (listView.getCheckedItemPosition() == position + 1)
            holder.listview_root.setBackgroundColor(checkedBG);

        final CommentBean comment = getList().get(position);

        UserBean user = comment.getUser();
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

        holder.content.setText(comment.getListViewSpannableString());

        holder.time.setTime(comment.getMills());
        if (holder.source != null)
            holder.source.setText(comment.getSourceString());

        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);

        CommentBean reply = comment.getReply_comment();
        if (holder.replyIV != null)
            holder.replyIV.setVisibility(View.GONE);
        if (reply != null && showOriStatus) {
            if (holder.repost_layout != null)
                holder.repost_layout.setVisibility(View.VISIBLE);
            holder.repost_flag.setVisibility(View.VISIBLE);
            holder.repost_content.setVisibility(View.VISIBLE);
            holder.repost_content.setText(reply.getListViewSpannableString());
            holder.repost_content.setTag(reply.getId());
        } else {

            MessageBean repost_msg = comment.getStatus();

            if (repost_msg != null && showOriStatus) {
                buildRepostContent(repost_msg, holder, position);
            } else {
                if (holder.repost_layout != null)
                    holder.repost_layout.setVisibility(View.GONE);
                holder.repost_flag.setVisibility(View.GONE);
                if (holder.replyIV != null) {
                    holder.replyIV.setVisibility(View.VISIBLE);
                    holder.replyIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), WriteReplyToCommentActivity.class);
                            intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                            intent.putExtra("msg", comment);
                            getActivity().startActivity(intent);
                        }
                    });
                }
            }

        }


    }

}


