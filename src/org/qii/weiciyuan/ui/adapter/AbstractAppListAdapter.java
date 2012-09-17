package org.qii.weiciyuan.ui.adapter;

import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ItemBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.ICommander;

import java.util.List;

/**
 * User: qii
 * Date: 12-9-15
 */
public abstract class AbstractAppListAdapter<T extends ItemBean> extends BaseAdapter {
    protected List<T> bean;
    protected Fragment activity;
    protected LayoutInflater inflater;
    protected ListView listView;
    protected ICommander commander;
    protected boolean showOriStatus = true;
    protected int checkedBG;
    protected int defaultBG;

    public AbstractAppListAdapter(Fragment activity, ICommander commander, List<T> bean, ListView listView, boolean showOriStatus) {
        this.bean = bean;
        this.commander = commander;
        this.inflater = activity.getActivity().getLayoutInflater();
        this.listView = listView;
        this.showOriStatus = showOriStatus;
        this.activity = activity;


        defaultBG = activity.getResources().getColor(R.color.transparent);

        int[] attrs = new int[]{R.attr.listview_checked_color};
        TypedArray ta = activity.getActivity().obtainStyledAttributes(attrs);
        checkedBG = ta.getColor(0, 430);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //mylayout time view position have a bug when set avatar view to gone,so init normal layout
        if (bean.get(position).getUser().getId().equals(GlobalContext.getInstance().getCurrentAccountId()) && GlobalContext.getInstance().isEnablePic()) {
            ViewHolder holder;
            if (convertView == null || convertView.getTag(R.drawable.app) == null) {
                convertView = initMylayout(parent);
                holder = buildHolder(convertView);
            } else {
                boolean enableBigPic = (Boolean) convertView.getTag(R.drawable.account_black);
                if (enableBigPic == GlobalContext.getInstance().getEnableBigPic()) {
                    holder = (ViewHolder) convertView.getTag(R.drawable.app);
                } else {
                    convertView = initMylayout(parent);
                    holder = buildHolder(convertView);
                }
            }
            convertView.setTag(R.drawable.app, holder);
            convertView.setTag(R.drawable.account_black, GlobalContext.getInstance().getEnableBigPic());
            bindViewData(holder, position);
            return convertView;
        }

        ViewHolder holder;
        if (convertView == null || convertView.getTag(R.drawable.ic_launcher) == null) {
            convertView = initNormallayout(parent);
            holder = buildHolder(convertView);
        } else {
            boolean enableBigPic = (Boolean) convertView.getTag(R.drawable.account_black);
            if (enableBigPic == GlobalContext.getInstance().getEnableBigPic()) {
                holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher);
            } else {
                convertView = initNormallayout(parent);
                holder = buildHolder(convertView);
            }
        }
        convertView.setTag(R.drawable.ic_launcher, holder);
        convertView.setTag(R.drawable.account_black, GlobalContext.getInstance().getEnableBigPic());
        bindViewData(holder, position);

        return convertView;
    }

    private View initMylayout(ViewGroup parent) {
        View convertView;
        if (GlobalContext.getInstance().getEnableBigPic()) {
            convertView = inflater.inflate(R.layout.fragment_listview_item_myself_big_pic_layout, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.fragment_listview_item_myself_layout, parent, false);
        }
        return convertView;
    }

    private View initNormallayout(ViewGroup parent) {
        View convertView;
        if (GlobalContext.getInstance().getEnableBigPic()) {
            convertView = inflater.inflate(R.layout.fragment_listview_item_big_pic_layout, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.fragment_listview_item_layout, parent, false);
        }
        return convertView;
    }


    private ViewHolder buildHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.username = (TextView) convertView.findViewById(R.id.username);
        TextPaint tp = holder.username.getPaint();
        tp.setFakeBoldText(true);
        holder.content = (TextView) convertView.findViewById(R.id.content);
        holder.repost_content = (TextView) convertView.findViewById(R.id.repost_content);
        holder.time = (TextView) convertView.findViewById(R.id.time);
        holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
        holder.content_pic = (ImageView) convertView.findViewById(R.id.content_pic);
        holder.repost_content_pic = (ImageView) convertView.findViewById(R.id.repost_content_pic);
        holder.listview_root = (RelativeLayout) convertView.findViewById(R.id.listview_root);
        return holder;
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
        if (position >= 0 && getList() != null && getList().size() > 0 && position < getList().size())
            return getList().get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (getList() != null && getList().size() > 0 && position < getList().size())
            return Long.valueOf(getList().get(position).getId());
        else
            return -1;
    }


    static class ViewHolder {
        TextView username;
        TextView content;
        TextView repost_content;
        TextView time;
        ImageView avatar;
        ImageView content_pic;
        ImageView repost_content_pic;
        RelativeLayout listview_root;
    }

    public void removeItem(final int postion) {
        if (postion >= 0 && postion < bean.size()) {

            Animation anim = AnimationUtils.loadAnimation(
                    activity.getActivity(), R.anim.account_delete_slide_out_right
            );

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    bean.remove(postion);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    AbstractAppListAdapter.this.notifyDataSetChanged();
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
            } else {
                bean.remove(postion);
                AbstractAppListAdapter.this.notifyDataSetChanged();
            }
        }
    }
}
