package org.qii.weiciyuan.ui.adapter;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.*;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.asyncdrawable.TimeLineBitmapDownloader;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.lib.MyURLSpan;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.actionmenu.StatusSingleChoiceModeListener;
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

    private ActionMode mActionMode;

    private Handler handler = new Handler();

    public StatusListAdapter(Fragment fragment, TimeLineBitmapDownloader commander, List<MessageBean> bean, ListView listView, boolean showOriStatus) {
        this(fragment, commander, bean, listView, showOriStatus, false);
    }

    public StatusListAdapter(Fragment fragment, TimeLineBitmapDownloader commander, List<MessageBean> bean, ListView listView, boolean showOriStatus, boolean pre) {
        super(fragment, commander, bean, listView, showOriStatus, pre);
    }

    public void onListViewScroll() {
        removeLongClick();
        removeClick();
        int state = ((AbstractTimeLineFragment) fragment).getListViewScrollState();
        if (state != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            LongClickableLinkMovementMethod.getInstance().removeLongClickCallback();

        }
        if (hasActionMode()) {
            int position = listView.getCheckedItemPosition();
            if (listView.getFirstVisiblePosition() > position || listView.getLastVisiblePosition() < position) {
                clearActionMode();
            }
        }
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

        holder.listview_root.setOnTouchListener(simpleOnTouchListener);

        holder.username.setOnTouchListener(simpleOnTouchListener);
        holder.time.setOnTouchListener(simpleOnTouchListener);

        holder.content.setOnTouchListener(onTouchListener);
        holder.repost_content.setOnTouchListener(onTouchListener);

        holder.listview_root.setClickable(false);
        holder.username.setClickable(false);
        holder.time.setClickable(false);
        holder.content.setClickable(false);
        holder.repost_content.setClickable(false);

        if (holder.content.getMovementMethod() != LongClickableLinkMovementMethod.getInstance())
            holder.content.setMovementMethod(LongClickableLinkMovementMethod.getInstance());
        if (holder.repost_content.getMovementMethod() != LongClickableLinkMovementMethod.getInstance())
            holder.repost_content.setMovementMethod(LongClickableLinkMovementMethod.getInstance());


        holder.time.setTime(msg.getMills());
        if (holder.source != null)
            holder.source.setText(Html.fromHtml(msg.getSource()).toString());

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


    //onTouchListener has some strange problem, when user click link, holder.listview_root may also receive a MotionEvent.ACTION_DOWN event
    //the background then changed
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            ViewHolder holder = getViewHolderByView(v);

            if (holder == null) {
                return false;
            }

            Layout layout = ((TextView) v).getLayout();

            int x = (int) event.getX();
            int y = (int) event.getY();
            int offset = 0;
            if (layout != null) {

                int line = layout.getLineForVertical(y);
                offset = layout.getOffsetForHorizontal(line, x);
            }

            TextView tv = (TextView) v;
            SpannableString value = SpannableString.valueOf(tv.getText());
            MyURLSpan[] urlSpans = value.getSpans(0, value.length(), MyURLSpan.class);
            boolean result = false;
            for (MyURLSpan urlSpan : urlSpans) {
                int start = value.getSpanStart(urlSpan);
                int end = value.getSpanEnd(urlSpan);
                if (start <= offset && offset <= end) {
                    result = true;
                    break;
                }
            }

            if (!result) {
                return simpleOnTouchListener.onTouch(v, event);
            } else {
                return false;
            }
        }
    };

    private float[] lastEvent = new float[2];


    private View.OnTouchListener simpleOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ViewHolder holder = getViewHolderByView(v);
            if (holder == null) {
                return false;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    holder.listview_root.setPressed(true);
                    isPressed = true;
                    final int position = listView.getPositionForView(v);
                    checkForClick(position);
                    lastEvent[0] = event.getX();
                    lastEvent[1] = event.getY();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    holder.listview_root.setPressed(false);
                    removeLongClick();
                    lastEvent = new float[2];
                    isPressed = false;
                    break;
                case MotionEvent.ACTION_UP:
                    holder.listview_root.setPressed(false);
                    removeLongClick();
                    lastEvent = new float[2];
                    isPressed = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float instanceX = Math.abs(lastEvent[0] - event.getX());
                    float instanceY = Math.abs(lastEvent[1] - event.getY());
                    if (Math.sqrt(Math.hypot(instanceX, instanceY)) > AppConfig.getScrollSlop()) {
                        removeClick();
                        LongClickableLinkMovementMethod.getInstance().removeLongClickCallback();
                    }
                    break;
            }
            return true;
        }
    };


    private boolean onItemLongClick(int position) {

        if (position == ListView.INVALID_POSITION) {
            return false;
        }

        int state = ((AbstractTimeLineFragment) fragment).getListViewScrollState();
        if (state != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            return false;
        }

        MessageBean msg = getList().get(position - 1);
        StatusSingleChoiceModeListener choiceModeListener = new StatusSingleChoiceModeListener(listView, StatusListAdapter.this, fragment, msg);
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }

        listView.setItemChecked(position, true);
        notifyDataSetChanged();
        mActionMode = getActivity().startActionMode(choiceModeListener);
        return true;
    }

    private void onItemClick(int position) {

        if (position == ListView.INVALID_POSITION) {
            return;
        }
        if (mActionMode != null) {
            clearActionMode();
            return;
        }
        MessageBean msg = getList().get(position - 1);
        if (!((AbstractTimeLineFragment) fragment).clearActionModeIfOpen()) {
            Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
            intent.putExtra("msg", msg);
            intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
            fragment.startActivityForResult(intent, 0);
        }
    }

    //when view is recycled by listview, need to catch exception
    private ViewHolder getViewHolderByView(View view) {
        try {
            final int position = listView.getPositionForView(view);
            if (position == ListView.INVALID_POSITION) {
                return null;
            }
            int wantedPosition = position - 1;
            int firstPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount();
            int wantedChild = wantedPosition - firstPosition;

            if (wantedChild < 0 || wantedChild >= listView.getChildCount()) {
                return null;
            }

            View wantedView = listView.getChildAt(wantedChild);
            ViewHolder holder = (ViewHolder) wantedView.getTag(R.drawable.ic_launcher + getItemViewType(position));
            return holder;
        } catch (NullPointerException e) {

        }
        return null;
    }

    public void clearActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
        if (listView.getCheckedItemCount() > 0) {
            listView.clearChoices();
            notifyDataSetChanged();
        }
    }

    private boolean hasActionMode() {
        return mActionMode != null;
    }

    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForPress mPendingCheckForPress;

    private boolean isPressed = false;

    private boolean isPressed() {
        return isPressed;
    }

    private void checkForLongClick(int position, int offset) {
        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress(position);
        } else {
            mPendingCheckForLongPress.setPosition(position);
        }
        handler.postDelayed(mPendingCheckForLongPress,
                ViewConfiguration.getLongPressTimeout() - offset);
    }

    private void removeLongClick() {
        if (mPendingCheckForLongPress != null)
            handler.removeCallbacks(mPendingCheckForLongPress);
    }

    class CheckForLongPress implements Runnable {

        int position;

        public void setPosition(int position) {
            this.position = position;
        }

        public CheckForLongPress(int position) {
            this.position = position;
        }

        public void run() {
            if (isPressed()) {
                onItemLongClick(position);
                Utility.vibrate(fragment.getActivity());
            }
        }

    }


    private void checkForClick(int position) {
        if (mPendingCheckForPress == null)
            mPendingCheckForPress = new CheckForPress(position);
        else
            mPendingCheckForPress.setPosition(position);

        handler.postDelayed(mPendingCheckForPress,
                ViewConfiguration.getTapTimeout());
    }

    private void removeClick() {
        if (mPendingCheckForPress != null)
            handler.removeCallbacks(mPendingCheckForPress);
    }

    class CheckForPress implements Runnable {

        int position;

        public void setPosition(int positon) {
            this.position = positon;
        }

        public CheckForPress(int position) {
            this.position = position;
        }

        public void run() {
            if (isPressed()) {
                checkForLongClick(position, ViewConfiguration.getTapTimeout());
            } else {
                onItemClick(position);
            }
        }

    }
}
