package org.qii.weiciyuan.ui.adapter;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.lib.AutoScrollListView;
import org.qii.weiciyuan.support.lib.TopTipBar;
import org.qii.weiciyuan.support.lib.VelocityListView;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.TimeLineUtility;
import org.qii.weiciyuan.support.utils.Utility;

import java.util.List;
import java.util.WeakHashMap;

/**
 * User: qii
 * Date: 12-8-19
 */
public class StatusListAdapter extends AbstractAppListAdapter<MessageBean> {

    private WeakHashMap<ViewHolder, Drawable> bg = new WeakHashMap<ViewHolder, Drawable>();

    private LongSparseArray<Integer> msgHeights = new LongSparseArray<Integer>();
    private LongSparseArray<Integer> msgWidths = new LongSparseArray<Integer>();

    private LongSparseArray<Integer> oriMsgHeights = new LongSparseArray<Integer>();
    private LongSparseArray<Integer> oriMsgWidths = new LongSparseArray<Integer>();

    private TopTipBar topTipBar;

    private Handler handler = new Handler();

    public StatusListAdapter(Fragment fragment, List<MessageBean> bean, ListView listView, boolean showOriStatus) {
        this(fragment, bean, listView, showOriStatus, false);
    }

    public StatusListAdapter(Fragment fragment, List<MessageBean> bean, ListView listView, boolean showOriStatus, boolean pre) {
        super(fragment, bean, listView, showOriStatus, pre);
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
                VelocityListView velocityListView = (VelocityListView) view;
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
//                }
            }

            private void handle(int position) {
                if (position > 0 && topTipBar != null && position < bean.size()) {
                    MessageBean next = bean.get(position);
                    if (next != null) {
                        MessageBean helperMsg = bean.get(position - 1);
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

        if (listView.getCheckedItemPosition() == position + listView.getHeaderViewsCount())
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
            boolean haveCachedHeight = msgHeights.get(msg.getIdLong()) != null;
            ViewGroup.LayoutParams layoutParams = holder.content.getLayoutParams();
            if (haveCachedHeight) {
                layoutParams.height = msgHeights.get(msg.getIdLong());
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            boolean haveCachedWidth = msgWidths.get(msg.getIdLong()) != null;
            if (haveCachedWidth) {
                layoutParams.width = msgWidths.get(msg.getIdLong());
            } else {
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            holder.content.requestLayout();
            holder.content.setText(msg.getListViewSpannableString());
            if (!haveCachedHeight) {
                msgHeights.append(msg.getIdLong(), layoutParams.height);
            }

            if (!haveCachedWidth) {
                msgWidths.append(msg.getIdLong(), layoutParams.width);
            }
        } else {
            TimeLineUtility.addJustHighLightLinks(msg);
            holder.content.setText(msg.getListViewSpannableString());
        }


        holder.time.setTime(msg.getMills());
        if (holder.source != null)
            holder.source.setText(msg.getSourceString());

        if (showOriStatus) {
            boolean checkRepostsCount = (msg.getReposts_count() != 0);
            boolean checkCommentsCount = (msg.getComments_count() != 0);
            boolean checkPic = (!TextUtils.isEmpty(msg.getThumbnail_pic())
                    || (msg.getRetweeted_status() != null
                    && !TextUtils.isEmpty(msg.getRetweeted_status().getThumbnail_pic())));
            checkPic = (checkPic && !SettingUtility.isEnablePic());
            boolean checkGps = (msg.getGeo() != null);

            if (!checkRepostsCount && !checkCommentsCount && !checkPic && !checkGps) {
                holder.count_layout.setVisibility(View.INVISIBLE);
            } else {
                holder.count_layout.setVisibility(View.VISIBLE);

                if (checkPic) {
                    holder.timeline_pic.setVisibility(View.VISIBLE);
                } else {
                    holder.timeline_pic.setVisibility(View.GONE);
                }

                if (checkGps) {
                    holder.timeline_gps.setVisibility(View.VISIBLE);
                } else {
                    holder.timeline_gps.setVisibility(View.INVISIBLE);
                }

                if (checkRepostsCount) {
                    holder.repost_count.setText(String.valueOf(msg.getReposts_count()));
                    holder.repost_count.setVisibility(View.VISIBLE);
                } else {
                    holder.repost_count.setVisibility(View.GONE);
                }

                if (checkCommentsCount) {
                    holder.comment_count.setText(String.valueOf(msg.getComments_count()));
                    holder.comment_count.setVisibility(View.VISIBLE);
                } else {
                    holder.comment_count.setVisibility(View.GONE);
                }
            }
        }

        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);
        holder.repost_content_pic_multi.setVisibility(View.GONE);

        holder.content_pic.setVisibility(View.GONE);
        holder.content_pic_multi.setVisibility(View.GONE);


        if (!TextUtils.isEmpty(msg.getThumbnail_pic())) {

            if (msg.isMultiPics()) {
                buildMultiPic(msg, holder.content_pic_multi);
            } else {
                buildPic(msg, holder.content_pic, position);
            }
        }

        MessageBean repost_msg = msg.getRetweeted_status();

        if (repost_msg != null && showOriStatus) {
            if (holder.repost_layout != null)
                holder.repost_layout.setVisibility(View.VISIBLE);
            holder.repost_flag.setVisibility(View.VISIBLE);
            //sina weibo official account can send repost message with picture, fuck sina weibo
            if (holder.content_pic.getVisibility() != View.GONE)
                holder.content_pic.setVisibility(View.GONE);
            buildRepostContent(msg, repost_msg, holder, position);


        } else {
            if (holder.repost_layout != null)
                holder.repost_layout.setVisibility(View.GONE);
            holder.repost_flag.setVisibility(View.GONE);
        }

    }


    private void buildRepostContent(MessageBean msg, final MessageBean repost_msg, ViewHolder holder, int position) {
        holder.repost_content.setVisibility(View.VISIBLE);
        if (!repost_msg.getId().equals((String) holder.repost_content.getTag())) {
            boolean haveCachedHeight = oriMsgHeights.get(msg.getIdLong()) != null;
            ViewGroup.LayoutParams layoutParams = holder.repost_content.getLayoutParams();
            if (haveCachedHeight) {
                layoutParams.height = oriMsgHeights.get(msg.getIdLong());
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            boolean haveCachedWidth = oriMsgWidths.get(msg.getIdLong()) != null;
            if (haveCachedWidth) {
                layoutParams.width = oriMsgWidths.get(msg.getIdLong());
            } else {
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            holder.repost_content.requestLayout();
            holder.repost_content.setText(repost_msg.getListViewSpannableString());

            if (!haveCachedHeight) {
                oriMsgHeights.append(msg.getIdLong(), layoutParams.height);
            }

            if (!haveCachedWidth) {
                oriMsgWidths.append(msg.getIdLong(), layoutParams.width);
            }

            holder.repost_content.setText(repost_msg.getListViewSpannableString());
            holder.repost_content.setTag(repost_msg.getId());
        }

        if (!TextUtils.isEmpty(repost_msg.getBmiddle_pic())) {
            if (repost_msg.isMultiPics()) {
                buildMultiPic(repost_msg, holder.repost_content_pic_multi);
            } else {
                buildPic(repost_msg, holder.repost_content_pic, position);
            }

        }
    }


}
