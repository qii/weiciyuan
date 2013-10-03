package org.qii.weiciyuan.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.DMUserBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.asyncdrawable.IWeiciyuanDrawable;
import org.qii.weiciyuan.support.asyncdrawable.TimeLineBitmapDownloader;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.lib.MyURLSpan;
import org.qii.weiciyuan.support.lib.TimeLineAvatarImageView;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.TimeLineUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import java.util.List;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMUserListAdapter extends BaseAdapter {
    private List<DMUserBean> bean;
    private Fragment fragment;
    private LayoutInflater inflater;
    private ListView listView;
    private TimeLineBitmapDownloader commander;


    public DMUserListAdapter(Fragment fragment, List<DMUserBean> bean, ListView listView) {
        this.bean = bean;
        this.commander = TimeLineBitmapDownloader.getInstance();
        this.inflater = fragment.getActivity().getLayoutInflater();
        this.listView = listView;
        this.fragment = fragment;

    }

    protected Activity getActivity() {
        return fragment.getActivity();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DMViewHolder holder = null;


        if (convertView == null || convertView.getTag() == null) {

            convertView = initSimpleLayout(parent);
            holder = buildHolder(convertView);
            convertView.setTag(R.drawable.ic_launcher + getItemViewType(position), holder);


        } else {
            holder = (DMViewHolder) convertView.getTag();
        }


        configViewFont(holder);
        bindViewData(holder, position);

        return convertView;
    }


    private View initSimpleLayout(ViewGroup parent) {
        View convertView;
        convertView = inflater.inflate(R.layout.dm_user_list_listview_item_layout, parent, false);

        return convertView;
    }


    private DMViewHolder buildHolder(View convertView) {
        DMViewHolder holder = new DMViewHolder();
        holder.username = (TextView) convertView.findViewById(R.id.username);
        TextPaint tp = holder.username.getPaint();
        tp.setFakeBoldText(true);
        holder.content = (TextView) convertView.findViewById(R.id.content);
        holder.time = (TextView) convertView.findViewById(R.id.time);
        holder.avatar = (TimeLineAvatarImageView) convertView.findViewById(R.id.avatar);
        return holder;
    }

    private void configViewFont(DMViewHolder holder) {

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

    protected void bindViewData(DMViewHolder holder, int position) {

        final DMUserBean msg = bean.get(position);
        UserBean user = msg.getUser();
        if (user != null) {
            holder.username.setVisibility(View.VISIBLE);
            buildUsername(holder, user);

            buildAvatar(holder.avatar, position, user);

        } else {
            holder.username.setVisibility(View.INVISIBLE);
            holder.avatar.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(msg.getListViewSpannableString())) {
            holder.content.setText(msg.getListViewSpannableString());
        } else {
            TimeLineUtility.addJustHighLightLinks(msg);
            holder.content.setText(msg.getListViewSpannableString());
        }

        bindOnTouchListener(holder);

        String time = msg.getListviewItemShowTime();

        if (!holder.time.getText().toString().equals(time)) {
            holder.time.setText(time);
        }
        holder.time.setTag(msg.getId());

    }

    private void buildUsername(DMViewHolder holder, UserBean user) {

        if (!TextUtils.isEmpty(user.getRemark())) {
            holder.username.setText(new StringBuilder(user.getScreen_name()).append("(").append(user.getRemark()).append(")").toString());
        } else {
            holder.username.setText(user.getScreen_name());
        }
    }

    private void bindOnTouchListener(DMViewHolder holder) {
        holder.username.setClickable(false);
        holder.time.setClickable(false);
        holder.content.setClickable(false);

        if (holder.content != null)
            holder.content.setOnTouchListener(onTouchListener);

    }

    protected List<DMUserBean> getList() {
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
        if (position >= 0 && getList() != null && getList().size() > 0 && position < getList().size())
            return getList().get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (getList() != null && getList().get(position) != null && getList().size() > 0 && position < getList().size())
            return Long.valueOf(getList().get(position).getId());
        else
            return -1;
    }

    protected void buildAvatar(ImageView view, int position, final UserBean user) {
        ((IWeiciyuanDrawable) view).checkVerified(user);
        String image_url = user.getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            view.setVisibility(View.VISIBLE);
            commander.downloadAvatar(view, user, (AbstractTimeLineFragment) fragment);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                    intent.putExtra("user", user);
                    getActivity().startActivity(intent);
                }
            });

        } else {
            view.setVisibility(View.GONE);
        }
    }


    private static class DMViewHolder {
        TextView username;
        TextView content;
        TextView time;
        TimeLineAvatarImageView avatar;

    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            DMViewHolder holder = getViewHolderByView(v);

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

            boolean hasActionMode = ((AbstractTimeLineFragment) fragment).hasActionMode();
            if (result && !hasActionMode) {
                return LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);
            } else {
                return false;
            }

        }
    };

    private DMViewHolder getViewHolderByView(View view) {
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

    private DMViewHolder getViewHolderByView(int position) {

        int wantedPosition = position - 1;
        int firstPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount();
        int wantedChild = wantedPosition - firstPosition;

        if (wantedChild < 0 || wantedChild >= listView.getChildCount()) {
            return null;
        }

        View wantedView = listView.getChildAt(wantedChild);
        DMViewHolder holder = (DMViewHolder) wantedView.getTag(R.drawable.ic_launcher + getItemViewType(position));
        return holder;

    }
}
