package org.qii.weiciyuan.ui.adapter;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.asyncdrawable.TimeLineBitmapDownloader;
import org.qii.weiciyuan.support.lib.MyLinkMovementMethod;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * User: qii
 * Date: 12-8-19
 */
public class StatusListAdapter extends AbstractAppListAdapter<MessageBean> {

    private Map<ViewHolder, Drawable> bg = new WeakHashMap<ViewHolder, Drawable>();

    private Map<String, Integer> msgHeights = new HashMap<String, Integer>();
    private Map<String, Integer> msgWidths = new HashMap<String, Integer>();

    private Map<String, Integer> oriMsgHeights = new HashMap<String, Integer>();
    private Map<String, Integer> oriMsgWidths = new HashMap<String, Integer>();

    public StatusListAdapter(Fragment fragment, TimeLineBitmapDownloader commander, List<MessageBean> bean, ListView listView, boolean showOriStatus) {
        super(fragment, commander, bean, listView, showOriStatus);
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
            boolean haveCachedHeight = msgHeights.containsKey(msg.getId());
            ViewGroup.LayoutParams layoutParams = holder.content.getLayoutParams();
            if (haveCachedHeight) {
                layoutParams.height = msgHeights.get(msg.getId());
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            boolean haveCachedWidth = msgWidths.containsKey(msg.getId());
            if (haveCachedWidth) {
                layoutParams.width = msgWidths.get(msg.getId());
            } else {
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            holder.content.requestLayout();
            holder.content.setText(msg.getListViewSpannableString());
            if (!haveCachedHeight) {
                msgHeights.put(msg.getId(), layoutParams.height);
            }

            if (!haveCachedWidth) {
                msgWidths.put(msg.getId(), layoutParams.width);
            }
        } else {
            ListViewTool.addJustHighLightLinks(msg);
            holder.content.setText(msg.getListViewSpannableString());
        }

        holder.listview_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((AbstractTimeLineFragment) fragment).clearActionModeIfOpen()) {
                    Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
                    intent.putExtra("msg", msg);
                    intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                    fragment.startActivityForResult(intent, 0);
                }
            }
        });


        holder.username.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                holder.listview_root.onTouchEvent(event);
                return false;
            }
        });
        holder.time.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                holder.listview_root.onTouchEvent(event);
                return false;
            }
        });

        //onTouchListener has some strange problem, when user click link, holder.listview_root may also receive a MotionEvent.ACTION_DOWN event
        //the background then changed
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextView tv = (TextView) v;
                int start = tv.getSelectionStart();
                int end = tv.getSelectionEnd();
                SpannableString completeText = (SpannableString) ((TextView) v).getText();
                boolean isNotLink = start == -1 || end == -1;

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_UP:
                        if (isNotLink && completeText.getSpanStart(this) == -1) {
                            holder.listview_root.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                            holder.listview_root.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                        }
                        break;
                }
                return false;
            }
        };

        holder.content.setOnTouchListener(onTouchListener);
        holder.repost_content.setOnTouchListener(onTouchListener);

        if (holder.content.getMovementMethod() != MyLinkMovementMethod.getInstance())
            holder.content.setMovementMethod(MyLinkMovementMethod.getInstance());
        if (holder.repost_content.getMovementMethod() != MyLinkMovementMethod.getInstance())
            holder.repost_content.setMovementMethod(MyLinkMovementMethod.getInstance());


        holder.time.setTime(msg.getMills());

        if (showOriStatus) {
            boolean checkRepostsCount = (msg.getReposts_count() != 0);
            boolean checkCommentsCount = (msg.getComments_count() != 0);
            boolean checkPic = (!TextUtils.isEmpty(msg.getThumbnail_pic())
                    || (msg.getRetweeted_status() != null
                    && !TextUtils.isEmpty(msg.getRetweeted_status().getThumbnail_pic())));
            checkPic = (checkPic && !SettingUtility.isEnablePic());
            boolean checkGps = (msg.getGeo() != null);

            if (!checkRepostsCount && !checkCommentsCount && !checkPic && !checkGps) {
                holder.count_layout.setVisibility(View.GONE);
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
                    holder.timeline_gps.setVisibility(View.GONE);
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
        holder.content_pic.setVisibility(View.GONE);


        if (!TextUtils.isEmpty(msg.getThumbnail_pic())) {
            buildPic(msg, holder.content_pic, position);

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
            boolean haveCachedHeight = oriMsgHeights.containsKey(msg.getId());
            ViewGroup.LayoutParams layoutParams = holder.repost_content.getLayoutParams();
            if (haveCachedHeight) {
                layoutParams.height = oriMsgHeights.get(msg.getId());
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            boolean haveCachedWidth = oriMsgWidths.containsKey(msg.getId());
            if (haveCachedWidth) {
                layoutParams.width = oriMsgWidths.get(msg.getId());
            } else {
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            holder.repost_content.requestLayout();
            holder.repost_content.setText(repost_msg.getListViewSpannableString());

            if (!haveCachedHeight) {
                oriMsgHeights.put(msg.getId(), layoutParams.height);
            }

            if (!haveCachedWidth) {
                oriMsgWidths.put(msg.getId(), layoutParams.width);
            }

            holder.repost_content.setText(repost_msg.getListViewSpannableString());
            holder.repost_content.setTag(repost_msg.getId());
        }

        if (!TextUtils.isEmpty(repost_msg.getBmiddle_pic())) {
            holder.repost_content_pic.setVisibility(View.VISIBLE);
            buildPic(repost_msg, holder.repost_content_pic, position);
        }
    }

}
