package org.qii.weiciyuan.ui.adapter;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ItemBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.asyncdrawable.IWeiciyuanDrawable;
import org.qii.weiciyuan.support.asyncdrawable.PictureBitmapDrawable;
import org.qii.weiciyuan.support.asyncdrawable.TimeLineBitmapDownloader;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.gallery.GalleryActivity;
import org.qii.weiciyuan.support.lib.ClickableTextViewMentionLinkOnTouchListener;
import org.qii.weiciyuan.support.lib.ListViewMiddleMsgLoadingView;
import org.qii.weiciyuan.support.lib.TimeLineAvatarImageView;
import org.qii.weiciyuan.support.lib.TimeTextView;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AnimationUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ThemeUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.support.utils.ViewUtility;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: qii
 * Date: 12-9-15
 */
public abstract class AbstractAppListAdapter<T extends ItemBean> extends BaseAdapter {

    protected List<T> bean;

    protected Fragment fragment;

    protected LayoutInflater inflater;

    protected ListView listView;

    protected boolean showOriStatus = true;

    protected int checkedBG;

    protected int defaultBG;

//    private final int TYPE_NORMAL = 0;
//    private final int TYPE_MYSELF = 1;
//    private final int TYPE_NORMAL_BIG_PIC = 2;
//    private final int TYPE_MYSELF_BIG_PIC = 3;
//    private final int TYPE_MIDDLE = 4;
//    private final int TYPE_SIMPLE = 5;

    private final int TYPE_NORMAL = 0;

    private final int TYPE_NORMAL_BIG_PIC = 1;

    private final int TYPE_MIDDLE = 2;

    private final int TYPE_SIMPLE = 3;

    public static final int NO_ITEM_ID = -1;

    private Set<Integer> tagIndexList = new HashSet<Integer>();

    private static final int PREF_LISTVIEW_ITEM_VIEW_COUNT = 6;

    private ArrayDeque<PrefView> prefNormalViews = new ArrayDeque<PrefView>(
            PREF_LISTVIEW_ITEM_VIEW_COUNT);

    private ArrayDeque<PrefView> prefBigPicViews = new ArrayDeque<PrefView>(
            PREF_LISTVIEW_ITEM_VIEW_COUNT);

    private int savedCurrentMiddleLoadingViewPosition
            = AbstractTimeLineFragment.NO_SAVED_CURRENT_LOADING_MSG_VIEW_POSITION;

    private class PrefView {

        View view;

        ViewHolder holder;
    }

    public void setSavedMiddleLoadingViewPosition(int position) {
        savedCurrentMiddleLoadingViewPosition = position;
    }

    public AbstractAppListAdapter(Fragment fragment, List<T> bean, ListView listView,
            boolean showOriStatus) {
        this(fragment, bean, listView, showOriStatus, false);
    }

    public AbstractAppListAdapter(Fragment fragment, List<T> bean, ListView listView,
            boolean showOriStatus, boolean pre) {
        if (showOriStatus) {
            listView.setDivider(null);
        }

        this.bean = bean;
        this.inflater = fragment.getActivity().getLayoutInflater();
        this.listView = listView;
        this.showOriStatus = showOriStatus;
        this.fragment = fragment;

        defaultBG = fragment.getResources().getColor(R.color.transparent);
        checkedBG = ThemeUtility.getColor(R.attr.listview_checked_color);

        if (pre) {
            for (int i = 0; i < PREF_LISTVIEW_ITEM_VIEW_COUNT; i++) {
                PrefView prefView = new PrefView();
                prefView.view = initNormalLayout(null);
                prefView.holder = buildHolder(prefView.view);
                prefNormalViews.add(prefView);
            }

            for (int i = 0; i < PREF_LISTVIEW_ITEM_VIEW_COUNT; i++) {
                PrefView prefView = new PrefView();
                prefView.view = initBigPicLayout(null);
                prefView.holder = buildHolder(prefView.view);
                prefBigPicViews.add(prefView);
            }
        }

        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                Integer index = (Integer) view.getTag(R.string.listview_index_tag);
                if (index == null) {
                    return;
                }

                for (Integer tag : tagIndexList) {

                    ViewHolder holder = (ViewHolder) view.getTag(tag);

                    if (holder != null) {
                        Drawable drawable = holder.avatar.getImageView().getDrawable();
                        clearAvatarBitmap(holder, drawable);
                        drawable = holder.content_pic.getImageView().getDrawable();
                        clearPictureBitmap(holder, drawable);
                        drawable = holder.repost_content_pic.getImageView().getDrawable();
                        clearRepostPictureBitmap(holder, drawable);

                        clearMultiPics(holder.content_pic_multi);
                        clearMultiPics(holder.repost_content_pic_multi);

                        if (!tag.equals(index)) {
                            holder.listview_root.removeAllViewsInLayout();
                            holder.listview_root = null;
                            view.setTag(tag, null);
                        }
                    }
                }
            }

            void clearMultiPics(GridLayout gridLayout) {
                if (gridLayout == null) {
                    return;
                }
                for (int i = 0; i < gridLayout.getChildCount(); i++) {
                    ImageView iv = (ImageView) gridLayout.getChildAt(i);
                    if (iv != null) {
                        iv.setImageDrawable(null);
                    }
                }
            }

            void clearAvatarBitmap(ViewHolder holder, Drawable drawable) {
                if (!(drawable instanceof PictureBitmapDrawable)) {
                    holder.avatar.setImageDrawable(null);
                    holder.avatar.getImageView().clearAnimation();
                }
            }

            void clearPictureBitmap(ViewHolder holder, Drawable drawable) {
                if (!(drawable instanceof PictureBitmapDrawable)) {
                    holder.content_pic.setImageDrawable(null);
                    holder.content_pic.getImageView().clearAnimation();
                }
            }

            void clearRepostPictureBitmap(ViewHolder holder, Drawable drawable) {
                if (!(drawable instanceof PictureBitmapDrawable)) {
                    holder.repost_content_pic.setImageDrawable(null);
                    holder.repost_content_pic.getImageView().clearAnimation();
                }
            }
        });
    }


    protected Activity getActivity() {
        return fragment.getActivity();
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {

        if (position >= bean.size()) {
            return -1;
        }

        if (bean.get(position) == null) {
            return TYPE_MIDDLE;
        }

        if (!showOriStatus) {
            return TYPE_SIMPLE;
        }

        if (SettingUtility.getEnableBigPic()) {
            return TYPE_NORMAL_BIG_PIC;
        } else {
            return TYPE_NORMAL;
        }

    }


    /**
     * use getTag(int) and setTag(int, final Object) to solve getItemViewType(int) bug.
     * When you use getItemViewType(int),getTag(),setTag() together, if getItemViewType(int) change
     * because
     * network switch to use another layout when you are scrolling listview, bug appears,the other
     * listviews in other tabs
     * (Actionbar tab navigation) will mix several layout up, for example, the correct layout
     * should
     * be TYPE_NORMAL_BIG_PIC,
     * but in the listview, you can see some row's layouts are TYPE_NORMAL, some are
     * TYPE_NORMAL_BIG_PIC. if you print
     * getItemViewType(int) value to the console,their are same type
     */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        PrefView prefView = null;

        if (convertView == null
                || convertView.getTag(R.drawable.ic_launcher + getItemViewType(position)) == null) {
            int itemViewType = getItemViewType(position);
            View view = null;
            switch (itemViewType) {
                case TYPE_SIMPLE:
                    view = initSimpleLayout(parent);
                    break;
                case TYPE_MIDDLE:
                    view = initMiddleLayout(parent);
                    break;
                case TYPE_NORMAL:
                    prefView = prefNormalViews.poll();
                    if (prefView != null) {
                        view = prefView.view;
                    }
                    if (view == null) {
                        view = initNormalLayout(parent);
                    }
                    break;
                case TYPE_NORMAL_BIG_PIC:
                    prefView = prefBigPicViews.poll();
                    if (prefView != null) {
                        view = prefView.view;
                    }
                    if (view == null) {
                        view = initBigPicLayout(parent);
                    }
                    break;
                default:
                    view = initNormalLayout(parent);
                    break;
            }

            convertView = view;
            if (itemViewType != TYPE_MIDDLE) {
                if (prefView == null) {
                    holder = buildHolder(convertView);
                } else {
                    holder = prefView.holder;
                }
                convertView.setTag(R.drawable.ic_launcher + getItemViewType(position), holder);
                convertView.setTag(R.string.listview_index_tag,
                        R.drawable.ic_launcher + getItemViewType(position));
                tagIndexList.add(R.drawable.ic_launcher + getItemViewType(position));
            }

        } else {
            holder = (ViewHolder) convertView
                    .getTag(R.drawable.ic_launcher + getItemViewType(position));
        }

        if (getItemViewType(position) != TYPE_MIDDLE) {
            configLayerType(holder);
            configViewFont(holder);
            bindViewData(holder, position);
            bindOnTouchListener(holder);
        } else {
            if (savedCurrentMiddleLoadingViewPosition == position + listView
                    .getHeaderViewsCount()) {
                ListViewMiddleMsgLoadingView loadingView
                        = (ListViewMiddleMsgLoadingView) convertView;
                loadingView.load();
            }
        }
        return convertView;
    }

    private void bindOnTouchListener(ViewHolder holder) {
        holder.listview_root.setClickable(false);
        holder.username.setClickable(false);
        holder.time.setClickable(false);
        holder.content.setClickable(false);
        holder.repost_content.setClickable(false);

        if (holder.content != null) {
            holder.content.setOnTouchListener(onTouchListener);
        }
        if (holder.repost_content != null) {
            holder.repost_content.setOnTouchListener(onTouchListener);
        }
    }

    private View initMiddleLayout(ViewGroup parent) {
        View convertView;
        convertView = inflater
                .inflate(R.layout.timeline_listview_item_middle_layout, parent, false);

        return convertView;
    }

    private View initSimpleLayout(ViewGroup parent) {
        View convertView;
        convertView = inflater
                .inflate(R.layout.timeline_listview_item_simple_layout, parent, false);

        return convertView;
    }

    private View initMylayout(ViewGroup parent) {
        View convertView;
        if (SettingUtility.getEnableBigPic()) {
            convertView = inflater
                    .inflate(R.layout.timeline_listview_item_big_pic_layout, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.timeline_listview_item_layout, parent, false);
        }
        return convertView;
    }

    private View initNormalLayout(ViewGroup parent) {
        return inflater.inflate(R.layout.timeline_listview_item_layout, parent, false);
    }

    private View initBigPicLayout(ViewGroup parent) {
        return inflater.inflate(R.layout.timeline_listview_item_big_pic_layout, parent, false);
    }

    //weibo image widgets and its forward weibo image widgets are the same
    private ViewHolder buildHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.username = ViewUtility.findViewById(convertView, R.id.username);
        TextPaint tp = holder.username.getPaint();
        if (tp != null) {
            tp.setFakeBoldText(true);
        }
        holder.content = ViewUtility.findViewById(convertView, R.id.content);
        holder.repost_content = ViewUtility.findViewById(convertView, R.id.repost_content);
        holder.time = ViewUtility.findViewById(convertView, R.id.time);
        holder.avatar = (TimeLineAvatarImageView) convertView.findViewById(R.id.avatar);

        holder.repost_content_pic = (IWeiciyuanDrawable) convertView
                .findViewById(R.id.repost_content_pic);
        holder.repost_content_pic_multi = ViewUtility
                .findViewById(convertView, R.id.repost_content__pic_multi);

        holder.content_pic = holder.repost_content_pic;
        holder.content_pic_multi = holder.repost_content_pic_multi;

        holder.listview_root = ViewUtility.findViewById(convertView, R.id.listview_root);
        holder.repost_layout = ViewUtility.findViewById(convertView, R.id.repost_layout);
        holder.repost_flag = ViewUtility.findViewById(convertView, R.id.repost_flag);
        holder.count_layout = ViewUtility.findViewById(convertView, R.id.count_layout);
        holder.repost_count = ViewUtility.findViewById(convertView, R.id.repost_count);
        holder.comment_count = ViewUtility.findViewById(convertView, R.id.comment_count);
        holder.timeline_gps = ViewUtility.findViewById(convertView, R.id.timeline_gps_iv);
        holder.timeline_pic = ViewUtility.findViewById(convertView, R.id.timeline_pic_iv);
        holder.replyIV = ViewUtility.findViewById(convertView, R.id.replyIV);
        holder.source = ViewUtility.findViewById(convertView, R.id.source);
        return holder;
    }

    private void configLayerType(ViewHolder holder) {

        boolean disableHardAccelerated = SettingUtility.disableHardwareAccelerated();
        if (!disableHardAccelerated) {
            return;
        }

        int currentWidgetLayerType = holder.username.getLayerType();

        if (View.LAYER_TYPE_SOFTWARE != currentWidgetLayerType) {
            holder.username.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            if (holder.content != null) {
                holder.content.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            if (holder.repost_content != null) {
                holder.repost_content.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            if (holder.time != null) {
                holder.time.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            if (holder.repost_count != null) {
                holder.repost_count.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            if (holder.comment_count != null) {
                holder.comment_count.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }

    }

    private void configViewFont(ViewHolder holder) {
        int prefFontSizeSp = SettingUtility.getFontSize();
        float currentWidgetTextSizePx;

        currentWidgetTextSizePx = holder.time.getTextSize();

        if (Utility.sp2px(prefFontSizeSp - 3) != currentWidgetTextSizePx) {
            holder.time.setTextSize(prefFontSizeSp - 3);
            if (holder.source != null) {
                holder.source.setTextSize(prefFontSizeSp - 3);
            }
        }

        currentWidgetTextSizePx = holder.content.getTextSize();

        if (Utility.sp2px(prefFontSizeSp) != currentWidgetTextSizePx) {
            holder.content.setTextSize(prefFontSizeSp);
            holder.username.setTextSize(prefFontSizeSp);
            holder.repost_content.setTextSize(prefFontSizeSp);

        }

        if (holder.repost_count != null) {
            currentWidgetTextSizePx = holder.repost_count.getTextSize();
            if (Utility.sp2px(prefFontSizeSp - 5) != currentWidgetTextSizePx) {
                holder.repost_count.setTextSize(prefFontSizeSp - 5);
            }
        }

        if (holder.comment_count != null) {
            currentWidgetTextSizePx = holder.comment_count.getTextSize();
            if (Utility.sp2px(prefFontSizeSp - 5) != currentWidgetTextSizePx) {
                holder.comment_count.setTextSize(prefFontSizeSp - 5);
            }
        }
    }

    protected abstract void bindViewData(ViewHolder holder, int position);

    protected List<T> getList() {
        return bean;
    }

    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {

        if (getList() != null) {
            return getList().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && getList() != null && getList().size() > 0 && position < getList()
                .size()) {
            return getList().get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (getList() != null && getList().get(position) != null && getList().size() > 0
                && position < getList().size()) {
            return Long.valueOf(getList().get(position).getId());
        } else {
            return NO_ITEM_ID;
        }
    }

    protected void buildAvatar(IWeiciyuanDrawable view, int position, final UserBean user) {
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
            TimeLineBitmapDownloader.getInstance()
                    .downloadAvatar(view, user, (AbstractTimeLineFragment) fragment);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    protected void buildMultiPic(final MessageBean msg, GridLayout gridLayout) {
        if (SettingUtility.isEnablePic()) {
            gridLayout.setVisibility(View.VISIBLE);

            int count = msg.getPicCount();
            for (int i = 0; i < count; i++) {
                IWeiciyuanDrawable pic = (IWeiciyuanDrawable) gridLayout.getChildAt(i);
                pic.setVisibility(View.VISIBLE);
                if (SettingUtility.getEnableBigPic()) {
                    TimeLineBitmapDownloader.getInstance()
                            .displayMultiPicture(pic, msg.getHighPicUrls().get(i),
                                    FileLocationMethod.picture_large,
                                    (AbstractTimeLineFragment) fragment);
                } else {
                    TimeLineBitmapDownloader.getInstance()
                            .displayMultiPicture(pic, msg.getThumbnailPicUrls().get(i),
                                    FileLocationMethod.picture_thumbnail,
                                    (AbstractTimeLineFragment) fragment);
                }

                final int finalI = i;
                pic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), GalleryActivity.class);
                        intent.putExtra("msg", msg);
                        intent.putExtra("position", finalI);
                        Bundle scaleBundle = ActivityOptions.makeScaleUpAnimation(
                                v, 0, 0, v.getWidth(), v.getHeight()).toBundle();
                        getActivity().startActivity(intent, scaleBundle);
                    }
                });

            }

            if (count < 9) {
                ImageView pic;
                switch (count) {
                    case 8:
                        pic = (ImageView) gridLayout.getChildAt(8);
                        pic.setVisibility(View.INVISIBLE);
                        break;
                    case 7:
                        for (int i = 8; i > 6; i--) {
                            pic = (ImageView) gridLayout.getChildAt(i);
                            pic.setVisibility(View.INVISIBLE);
                        }
                        break;
                    case 6:
                        for (int i = 8; i > 5; i--) {
                            pic = (ImageView) gridLayout.getChildAt(i);
                            pic.setVisibility(View.GONE);
                        }

                        break;
                    case 5:
                        for (int i = 8; i > 5; i--) {
                            pic = (ImageView) gridLayout.getChildAt(i);
                            pic.setVisibility(View.GONE);
                        }
                        pic = (ImageView) gridLayout.getChildAt(5);
                        pic.setVisibility(View.INVISIBLE);
                        break;
                    case 4:
                        for (int i = 8; i > 5; i--) {
                            pic = (ImageView) gridLayout.getChildAt(i);
                            pic.setVisibility(View.GONE);
                        }
                        pic = (ImageView) gridLayout.getChildAt(5);
                        pic.setVisibility(View.INVISIBLE);
                        pic = (ImageView) gridLayout.getChildAt(4);
                        pic.setVisibility(View.INVISIBLE);
                        break;
                    case 3:
                        for (int i = 8; i > 2; i--) {
                            pic = (ImageView) gridLayout.getChildAt(i);
                            pic.setVisibility(View.GONE);
                        }
                        break;
                    case 2:
                        for (int i = 8; i > 2; i--) {
                            pic = (ImageView) gridLayout.getChildAt(i);
                            pic.setVisibility(View.GONE);
                        }
                        pic = (ImageView) gridLayout.getChildAt(2);
                        pic.setVisibility(View.INVISIBLE);
                        break;


                }

            }


        } else {
            gridLayout.setVisibility(View.GONE);
        }

    }

    protected void buildPic(final MessageBean msg, final IWeiciyuanDrawable view, int position) {
        if (SettingUtility.isEnablePic()) {
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ImageView imageView = view.getImageView();

                    Intent intent = new Intent(getActivity(), GalleryActivity.class);
                    intent.putExtra("msg", msg);

                    Rect rect = AnimationUtility.getBitmapRectFromImageView(imageView);

                    if (rect != null) {
                        intent.putExtra("rect", rect);
                    }

                    Bundle scaleBundle = ActivityOptions.makeScaleUpAnimation(
                            v, 0, 0, v.getWidth(), v.getHeight()).toBundle();
                    getActivity().startActivity(intent, scaleBundle);
                }
            });
            buildPic(msg, view);


        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void buildPic(final MessageBean msg, ImageView view) {
        view.setVisibility(View.VISIBLE);
        TimeLineBitmapDownloader.getInstance()
                .downContentPic(view, msg, (AbstractTimeLineFragment) fragment);
    }

    private void buildPic(final MessageBean msg, IWeiciyuanDrawable view) {
        view.setVisibility(View.VISIBLE);
        TimeLineBitmapDownloader.getInstance()
                .downContentPic(view, msg, (AbstractTimeLineFragment) fragment);
    }

    protected void buildRepostContent(final MessageBean repost_msg, ViewHolder holder,
            int position) {
        holder.repost_content.setVisibility(View.VISIBLE);

        boolean isSameTag = repost_msg.getId().equals((String) holder.repost_content.getTag());

        if (!isSameTag) {
            holder.repost_content.setText(repost_msg.getListViewSpannableString());
            holder.repost_content.setTag(repost_msg.getId());
        }

        if (!TextUtils.isEmpty(repost_msg.getBmiddle_pic())) {
            holder.repost_content_pic.setVisibility(View.VISIBLE);
            buildPic(repost_msg, holder.repost_content_pic, position);
        }
    }


    public static class ViewHolder {

        TextView username;

        TextView content;

        TextView repost_content;

        TimeTextView time;

        IWeiciyuanDrawable avatar;

        IWeiciyuanDrawable content_pic;

        GridLayout content_pic_multi;

        IWeiciyuanDrawable repost_content_pic;

        GridLayout repost_content_pic_multi;

        ViewGroup listview_root;

        View repost_layout;

        View repost_flag;

        LinearLayout count_layout;

        TextView repost_count;

        TextView comment_count;

        TextView source;

        ImageView timeline_gps;

        ImageView timeline_pic;

        ImageView replyIV;
    }

    public void removeItem(final int postion) {
        if (postion >= 0 && postion < bean.size()) {
            AppLogger.e("1");
            Animation anim = AnimationUtils.loadAnimation(
                    fragment.getActivity(), R.anim.account_delete_slide_out_right
            );

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                    AppLogger.e("4");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    bean.remove(postion);
                    AbstractAppListAdapter.this.notifyDataSetChanged();
                    AppLogger.e("5");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            int positonInListView = postion + 1;
            int start = listView.getFirstVisiblePosition();
            int end = listView.getLastVisiblePosition();

            if (positonInListView >= start && positonInListView <= end) {
                int positionInCurrentScreen = postion - start;
                listView.getChildAt(positionInCurrentScreen + 1).startAnimation(anim);
                AppLogger.e("2");
            } else {
                bean.remove(postion);
                AbstractAppListAdapter.this.notifyDataSetChanged();
                AppLogger.e("3");
            }

        }
    }


    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        ClickableTextViewMentionLinkOnTouchListener listener
                = new ClickableTextViewMentionLinkOnTouchListener();

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            ViewHolder holder = getViewHolderByView(v);

            if (holder == null) {
                return false;
            }

            boolean hasActionMode = ((AbstractTimeLineFragment) fragment).hasActionMode();

            return !hasActionMode && listener.onTouch(v, event);


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
        ViewHolder holder = (ViewHolder) wantedView
                .getTag(R.drawable.ic_launcher + getItemViewType(wantedPosition));
        return holder;

    }

}
