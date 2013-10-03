package org.qii.weiciyuan.support.utils;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.widget.TextView;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.support.database.FilterDBTask;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.lib.MyURLSpan;
import org.qii.weiciyuan.support.lib.WeiboPatterns;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

/**
 * User: qii
 * Date: 12-8-29
 * build emotions and clickable string in other threads except UI thread, improve listview scroll performance
 */
public class TimeLineUtility {

    private TimeLineUtility() {
    }


    public static void addLinks(TextView view) {
        CharSequence content = view.getText();
        view.setText(convertNormalStringToSpannableString(content.toString()));
        if (view.getLinksClickable()) {
            view.setMovementMethod(LongClickableLinkMovementMethod.getInstance());
        }
    }

    private static SpannableString convertNormalStringToSpannableString(String txt) {
        //hack to fix android imagespan bug,see http://stackoverflow.com/questions/3253148/imagespan-is-cut-off-incorrectly-aligned
        //if string only contains emotion tags,add a empty char to the end
        String hackTxt;
        if (txt.startsWith("[") && txt.endsWith("]")) {
            hackTxt = txt + " ";
        } else {
            hackTxt = txt;
        }
        SpannableString value = SpannableString.valueOf(hackTxt);
        Linkify.addLinks(value, WeiboPatterns.MENTION_URL, WeiboPatterns.MENTION_SCHEME);
        Linkify.addLinks(value, WeiboPatterns.WEB_URL, WeiboPatterns.WEB_SCHEME);
        Linkify.addLinks(value, WeiboPatterns.TOPIC_URL, WeiboPatterns.TOPIC_SCHEME);

        URLSpan[] urlSpans = value.getSpans(0, value.length(), URLSpan.class);
        MyURLSpan weiboSpan = null;
        for (URLSpan urlSpan : urlSpans) {
            weiboSpan = new MyURLSpan(urlSpan.getURL());
            int start = value.getSpanStart(urlSpan);
            int end = value.getSpanEnd(urlSpan);
            value.removeSpan(urlSpan);
            value.setSpan(weiboSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        TimeLineUtility.addEmotions(value);
        return value;
    }

    public static void addJustHighLightLinks(MessageBean bean) {
        bean.setListViewSpannableString(convertNormalStringToSpannableString(bean.getText()));
        bean.getSourceString();

        if (bean.getRetweeted_status() != null) {
            bean.getRetweeted_status().setListViewSpannableString(buildOriWeiboSpannalString(bean.getRetweeted_status()));
            bean.getRetweeted_status().getSourceString();
        }
    }

    private static SpannableString buildOriWeiboSpannalString(MessageBean oriMsg) {
        String name = "";
        UserBean oriUser = oriMsg.getUser();
        if (oriUser != null) {
            name = oriUser.getScreen_name();
            if (TextUtils.isEmpty(name)) {
                name = oriUser.getId();
            }
        }

        SpannableString value;

        if (!TextUtils.isEmpty(name)) {
            value = TimeLineUtility.convertNormalStringToSpannableString("@" + name + "：" + oriMsg.getText());
        } else {
            value = TimeLineUtility.convertNormalStringToSpannableString(oriMsg.getText());
        }
        return value;
    }

    public static void addJustHighLightLinks(CommentBean bean) {

        bean.setListViewSpannableString(TimeLineUtility.convertNormalStringToSpannableString(bean.getText()));
        bean.getSourceString();

        if (bean.getStatus() != null) {
            bean.getStatus().setListViewSpannableString(buildOriWeiboSpannalString(bean.getStatus()));
        }

        if (bean.getReply_comment() != null) {
            addJustHighLightLinksOnlyReplyComment(bean.getReply_comment());
        }
    }

    private static void addJustHighLightLinksOnlyReplyComment(CommentBean bean) {
        String name = "";
        UserBean reUser = bean.getUser();
        if (reUser != null) {
            name = reUser.getScreen_name();
        }

        SpannableString value;

        if (!TextUtils.isEmpty(name)) {
            value = TimeLineUtility.convertNormalStringToSpannableString("@" + name + "：" + bean.getText());
        } else {
            value = TimeLineUtility.convertNormalStringToSpannableString(bean.getText());
        }

        bean.setListViewSpannableString(value);
    }

    public static void addJustHighLightLinks(DMUserBean bean) {
        bean.setListViewSpannableString(TimeLineUtility.convertNormalStringToSpannableString(bean.getText()));
    }

    public static void addJustHighLightLinks(DMBean bean) {
        bean.setListViewSpannableString(TimeLineUtility.convertNormalStringToSpannableString(bean.getText()));
    }

    public static void filterHomeTimeLineSinaWeiboAd(MessageListBean value) {

        List<MessageBean> msgList = value.getItemList();
        Iterator<MessageBean> iterator = msgList.iterator();

        while (iterator.hasNext()) {
            MessageBean msg = iterator.next();
            UserBean user = msg.getUser();
            if (user == null)
                continue;
            if (user.isEnterpriseV() && !user.isFollowing()) {
                if (!user.getId().equalsIgnoreCase(GlobalContext.getInstance().getCurrentAccountId())) {
                    iterator.remove();
                    value.removedCountPlus();
                }
            }
        }
    }

    public static void filterMessage(MessageListBean value) {

        List<MessageBean> msgList = value.getItemList();
        Iterator<MessageBean> iterator = msgList.iterator();

        List<String> keywordFilter = FilterDBTask.getFilterKeywordList(FilterDBTask.TYPE_KEYWORD);
        List<String> userFilter = FilterDBTask.getFilterKeywordList(FilterDBTask.TYPE_USER);
        List<String> topicFilter = FilterDBTask.getFilterKeywordList(FilterDBTask.TYPE_TOPIC);
        List<String> sourceFilter = FilterDBTask.getFilterKeywordList(FilterDBTask.TYPE_SOURCE);


        while (iterator.hasNext()) {
            MessageBean msg = iterator.next();
            if (msg.getUser() == null) {
                iterator.remove();
                value.removedCountPlus();
            } else if (SettingUtility.isEnableFilter() && TimeLineUtility.haveFilterWord(msg, keywordFilter, userFilter, topicFilter, sourceFilter)) {
                iterator.remove();
                value.removedCountPlus();
            } else {
                msg.getListViewSpannableString();
                TimeUtility.dealMills(msg);
            }
        }
    }

    public static boolean haveFilterWord(MessageBean content, List<String> keywordFilter
            , List<String> userFilter, List<String> topicFilter, List<String> sourceFilter) {

        //if this message is sent myself, ignore it;
        if (content.getUser().getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
            return false;
        }

        boolean hasOriMessage = content.getRetweeted_status() != null;
        MessageBean oriMessage = content.getRetweeted_status();

        for (String filterWord : keywordFilter) {

            if (content.getText().contains(filterWord)) {
                return true;
            }

            if (hasOriMessage && oriMessage.getText().contains(filterWord)) {
                return true;
            }

        }

        for (String filterWord : userFilter) {

            if (content.getUser() != null && content.getUser().getScreen_name().equals(filterWord)) {
                return true;
            }


            if (hasOriMessage && oriMessage.getUser() != null
                    && oriMessage.getUser().getScreen_name().equals(filterWord)) {
                return true;
            }
        }

        for (String filterWord : topicFilter) {


            Matcher localMatcher = WeiboPatterns.TOPIC_URL.matcher(content.getText());

            while (localMatcher.find()) {
                String str2 = localMatcher.group(0);
                if (TextUtils.isEmpty(str2) || str2.length() < 3) {
                    continue;
                }

                if (filterWord.equals(str2.substring(1, str2.length() - 1))) {
                    return true;
                }

            }


            if (content.getRetweeted_status() != null) {

                localMatcher = WeiboPatterns.TOPIC_URL.matcher(content.getRetweeted_status().getText());

                while (localMatcher.find()) {
                    String str2 = localMatcher.group(0);
                    if (TextUtils.isEmpty(str2) || str2.length() < 3) {
                        continue;
                    }

                    if (filterWord.equals(str2.substring(1, str2.length() - 1))) {
                        return true;
                    }

                }
            }

        }

        for (String filterWord : sourceFilter) {

            if (filterWord.equals(content.getSourceString())) {
                return true;
            }


            if (hasOriMessage && filterWord.equals(content.getRetweeted_status().getSourceString())) {
                return true;
            }
        }

        return false;
    }


    private static void addEmotions(SpannableString value) {
        Matcher localMatcher = WeiboPatterns.EMOTION_URL.matcher(value);
        while (localMatcher.find()) {
            String str2 = localMatcher.group(0);
            int k = localMatcher.start();
            int m = localMatcher.end();
            if (m - k < 8) {
                Bitmap bitmap = GlobalContext.getInstance().getEmotionsPics().get(str2);
                if (bitmap != null) {
                    ImageSpan localImageSpan = new ImageSpan(GlobalContext.getInstance().getActivity(), bitmap, ImageSpan.ALIGN_BASELINE);
                    value.setSpan(localImageSpan, k, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            }
        }
    }
}
