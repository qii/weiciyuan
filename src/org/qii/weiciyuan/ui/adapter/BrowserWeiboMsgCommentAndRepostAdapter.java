package org.qii.weiciyuan.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.asyncdrawable.TimeLineBitmapDownloader;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.lib.MyURLSpan;
import org.qii.weiciyuan.support.lib.TimeLineAvatarImageView;
import org.qii.weiciyuan.support.lib.TimeTextView;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.send.WriteReplyToCommentActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * User: qii
 * Date: 13-6-16
 */
public class BrowserWeiboMsgCommentAndRepostAdapter extends BaseAdapter {

    private boolean isCommentList = true;

    private List<CommentBean> commentListBean;
    private List<MessageBean> repostListBean;

    private Fragment fragment;
    private ListView listView;

    private int checkedBG;
    private int defaultBG;
    private LayoutInflater inflater;

    private Map<BrowserWeiboMsgCommentAndRepostAdapter.ViewHolder, Drawable> bg = new WeakHashMap<BrowserWeiboMsgCommentAndRepostAdapter.ViewHolder, Drawable>();


    public BrowserWeiboMsgCommentAndRepostAdapter(Fragment fragment, ListView listView
            , List<CommentBean> commentListBean, List<MessageBean> repostListBean) {

        this.fragment = fragment;
        this.listView = listView;
        this.commentListBean = commentListBean;
        this.repostListBean = repostListBean;
        this.inflater = fragment.getActivity().getLayoutInflater();

        this.defaultBG = fragment.getResources().getColor(R.color.transparent);

        int[] attrs = new int[]{R.attr.listview_checked_color};
        TypedArray ta = fragment.getActivity().obtainStyledAttributes(attrs);
        this.checkedBG = ta.getColor(0, 430);
    }

    protected Activity getActivity() {
        return fragment.getActivity();
    }

    public void switchToRepostType() {
        this.isCommentList = false;
        notifyDataSetChanged();
    }

    public void switchToCommentType() {
        this.isCommentList = true;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (isCommentList) {
            return commentListBean.size();
        } else {
            return repostListBean.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (isCommentList) {
            return commentListBean.get(position);
        } else {
            return repostListBean.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        if (isCommentList) {
            return commentListBean.get(position).getIdLong();
        } else {
            return repostListBean.get(position).getIdLong();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null || convertView.getTag() == null) {
            convertView = initSimpleLayout(parent);
            holder = buildHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        configLayerType(holder);
        configViewFont(holder);
        bindViewData(holder, position);
        bindOnTouchListener(holder);

        return convertView;
    }


    public void bindViewData(ViewHolder holder, int position) {
        if (isCommentList)
            bindCommentData(holder, position);
        else
            bindRepostData(holder, position);
    }


    private void bindCommentData(ViewHolder holder, int position) {
        Drawable drawable = bg.get(holder);
        if (drawable != null) {
            holder.listview_root.setBackgroundDrawable(drawable);

        } else {
            drawable = holder.listview_root.getBackground();
            bg.put(holder, drawable);
        }

        if (listView.getCheckedItemPosition() == position + listView.getHeaderViewsCount())
            holder.listview_root.setBackgroundColor(checkedBG);

        final CommentBean comment = (CommentBean) getItem(position);

        UserBean user = comment.getUser();
        if (user != null) {
            holder.username.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(user.getRemark())) {
                holder.username.setText(new StringBuilder(user.getScreen_name()).append("(").append(user.getRemark()).append(")").toString());
            } else {
                holder.username.setText(user.getScreen_name());
            }
            if (!SettingUtility.getEnableCommentRepostListAvatar()) {
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

        holder.reply.setVisibility(View.VISIBLE);
        holder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WriteReplyToCommentActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("msg", comment);
                getActivity().startActivity(intent);
            }
        });
    }

    private void bindRepostData(ViewHolder holder, int position) {
        Drawable drawable = bg.get(holder);
        if (drawable != null) {
            holder.listview_root.setBackgroundDrawable(drawable);

        } else {
            drawable = holder.listview_root.getBackground();
            bg.put(holder, drawable);
        }

        if (listView.getCheckedItemPosition() == position + listView.getHeaderViewsCount())
            holder.listview_root.setBackgroundColor(checkedBG);

        final MessageBean msg = (MessageBean) getItem(position);

        UserBean user = msg.getUser();
        if (user != null) {
            holder.username.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(user.getRemark())) {
                holder.username.setText(new StringBuilder(user.getScreen_name()).append("(").append(user.getRemark()).append(")").toString());
            } else {
                holder.username.setText(user.getScreen_name());
            }
            if (!SettingUtility.getEnableCommentRepostListAvatar()) {
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
        holder.reply.setVisibility(View.GONE);


    }

    private View initSimpleLayout(ViewGroup parent) {
        View convertView;
        convertView = inflater.inflate(R.layout.timeline_listview_item_simple_layout, parent, false);

        return convertView;
    }

    private void bindOnTouchListener(ViewHolder holder) {
        holder.listview_root.setClickable(false);
        holder.username.setClickable(false);
        holder.time.setClickable(false);
        holder.content.setClickable(false);

        if (holder.content != null)
            holder.content.setOnTouchListener(onTouchListener);

    }


    private ViewHolder buildHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.username = (TextView) convertView.findViewById(R.id.username);
        TextPaint tp = holder.username.getPaint();
        tp.setFakeBoldText(true);
        holder.content = (TextView) convertView.findViewById(R.id.content);
        holder.time = (TimeTextView) convertView.findViewById(R.id.time);
        holder.avatar = (TimeLineAvatarImageView) convertView.findViewById(R.id.avatar);
        holder.listview_root = (RelativeLayout) convertView.findViewById(R.id.listview_root);
        holder.reply = (ImageView) convertView.findViewById(R.id.replyIV);
        return holder;
    }

    private void configLayerType(ViewHolder holder) {

        boolean disableHardAccelerated = SettingUtility.disableHardwareAccelerated();
        if (!disableHardAccelerated)
            return;

        int currentWidgetLayerType = holder.username.getLayerType();

        if (View.LAYER_TYPE_SOFTWARE != currentWidgetLayerType) {
            holder.username.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            if (holder.content != null)
                holder.content.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            if (holder.time != null)
                holder.time.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        }

    }

    private void configViewFont(ViewHolder holder) {
        int prefFontSizeSp = SettingUtility.getFontSize();
        float currentWidgetTextSizePx;

        currentWidgetTextSizePx = holder.time.getTextSize();

        if (Utility.sp2px(prefFontSizeSp - 3) != currentWidgetTextSizePx) {
            holder.time.setTextSize(prefFontSizeSp - 3);

        }

        currentWidgetTextSizePx = holder.content.getTextSize();


        if (Utility.sp2px(prefFontSizeSp) != currentWidgetTextSizePx) {
            holder.content.setTextSize(prefFontSizeSp);
            holder.username.setTextSize(prefFontSizeSp);

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

            //            boolean hasActionMode = ((BrowserWeiboMsgFragment) fragment).hasActionMode();
            boolean hasActionMode = false;
            if (result && !hasActionMode) {
                return LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);
            } else {
                if (event.getActionMasked() == MotionEvent.ACTION_CANCEL)
                    LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);
                return false;
            }

        }
    };


    //when view is recycled by listview, need to catch exception
    private ViewHolder getViewHolderByView(View view) {
        try {
            final int position = listView.getPositionForView(view);
            if (position == ListView.INVALID_POSITION) {
                return null;
            }
            return getViewHolderByView(position);
        } catch (NullPointerException e) {

        }
        return null;
    }

    private ViewHolder getViewHolderByView(int position) {

        int wantedPosition = position - listView.getHeaderViewsCount();
        int firstPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount();
        int wantedChild = wantedPosition - firstPosition;

        if (wantedChild < 0 || wantedChild >= listView.getChildCount()) {
            return null;
        }

        View wantedView = listView.getChildAt(wantedChild);
        ViewHolder holder = (ViewHolder) wantedView.getTag();
        return holder;

    }

    private static class ViewHolder {
        RelativeLayout listview_root;

        TextView username;
        TextView content;
        TimeTextView time;
        TimeLineAvatarImageView avatar;
        ImageView reply;
    }

    protected void buildAvatar(TimeLineAvatarImageView view, int position, final UserBean user) {
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("user", user);
                getActivity().startActivity(intent);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                UserDialog dialog = new UserDialog(user);
                dialog.show(fragment.getFragmentManager(), "");
                return true;
            }
        });
        view.checkVerified(user);
        buildAvatar(view.getImageView(), position, user);
    }

    protected void buildAvatar(ImageView view, int position, final UserBean user) {
        String image_url = user.getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            view.setVisibility(View.VISIBLE);
            TimeLineBitmapDownloader.getInstance().downloadAvatar(view, user, false);
        } else {
            view.setVisibility(View.GONE);
        }
    }
}
