package org.qii.weiciyuan.ui.adapter;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.asyncdrawable.TimeLineBitmapDownloader;
import org.qii.weiciyuan.support.lib.MyLinkMovementMethod;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.actionmenu.CommentFloatingMenu;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.send.WriteReplyToCommentActivity;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * User: qii
 * Date: 12-9-8
 */
public class CommentListAdapter extends AbstractAppListAdapter<CommentBean> {

    private Drawable replyPic = null;
    private Drawable commentPic = null;

    private Map<ViewHolder, Drawable> bg = new WeakHashMap<ViewHolder, Drawable>();

    public CommentListAdapter(Fragment fragment, TimeLineBitmapDownloader commander, List<CommentBean> bean, ListView listView, boolean showOriStatus) {
        super(fragment, commander, bean, listView, showOriStatus);

        int[] attrs = new int[]{R.attr.timeline_reply_flag};
        TypedArray ta = fragment.getActivity().obtainStyledAttributes(attrs);
        replyPic = ta.getDrawable(0);

        attrs = new int[]{R.attr.timeline_comment_flag};
        ta = fragment.getActivity().obtainStyledAttributes(attrs);
        commentPic = ta.getDrawable(0);
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

        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);

        if (holder.content.getMovementMethod() != MyLinkMovementMethod.getInstance())
            holder.content.setMovementMethod(MyLinkMovementMethod.getInstance());
        if (holder.repost_content.getMovementMethod() != MyLinkMovementMethod.getInstance())
            holder.repost_content.setMovementMethod(MyLinkMovementMethod.getInstance());

        CommentBean reply = comment.getReply_comment();
        if (holder.replyIV != null)
            holder.replyIV.setVisibility(View.GONE);
        if (reply != null && showOriStatus) {
            holder.repost_layout.setVisibility(View.VISIBLE);
            holder.repost_flag.setVisibility(View.VISIBLE);
            holder.repost_content.setVisibility(View.VISIBLE);
            holder.repost_content.setText(reply.getListViewReplySpannableString());
        } else {

            MessageBean repost_msg = comment.getStatus();

            if (repost_msg != null && showOriStatus) {
                buildRepostContent(repost_msg, holder, position);
            } else {
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

        if (showOriStatus) {
            holder.listview_root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((AbstractTimeLineFragment) fragment).clearActionModeIfOpen()) {
                        CommentFloatingMenu menu = new CommentFloatingMenu(comment);
                        menu.show(fragment.getFragmentManager(), "");
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

            holder.content.setMovementMethod(MyLinkMovementMethod.getInstance());
            holder.repost_content.setMovementMethod(MyLinkMovementMethod.getInstance());

            //onTouchListener has some strange problem, when user click link, holder.listview_root may also receive a MotionEvent.ACTION_DOWN event
            //the background then changed
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView) v;
                    int start = tv.getSelectionStart();
                    int end = tv.getSelectionEnd();
                    SpannableString completeText = (SpannableString) ((TextView) v).getText();
                    boolean isNotLink = start == -1 || end == -1;
                    if (isNotLink && completeText.getSpanStart(this) == -1) {
                        holder.listview_root.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                        holder.listview_root.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));

                    }
                }
            };

            holder.content.setOnClickListener(onClickListener);
            holder.repost_content.setOnClickListener(onClickListener);
        }
    }

}


