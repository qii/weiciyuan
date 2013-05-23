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
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.lib.MyURLSpan;
import org.qii.weiciyuan.support.lib.WeiboPatterns;

import java.util.List;
import java.util.regex.Matcher;

/**
 * User: qii
 * Date: 12-8-29
 * build emotions and clickable string in other threads except UI thread, improve listview scroll performance
 */
public class ListViewTool {

    private ListViewTool() {
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

        ListViewTool.addEmotions(value);
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
            value = ListViewTool.convertNormalStringToSpannableString("@" + name + "：" + oriMsg.getText());
        } else {
            value = ListViewTool.convertNormalStringToSpannableString(oriMsg.getText());
        }
        return value;
    }

    public static void addJustHighLightLinks(CommentBean bean) {

        bean.setListViewSpannableString(ListViewTool.convertNormalStringToSpannableString(bean.getText()));
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
            value = ListViewTool.convertNormalStringToSpannableString("@" + name + "：" + bean.getText());
        } else {
            value = ListViewTool.convertNormalStringToSpannableString(bean.getText());
        }

        bean.setListViewSpannableString(value);
    }

    public static void addJustHighLightLinks(DMUserBean bean) {
        bean.setListViewSpannableString(ListViewTool.convertNormalStringToSpannableString(bean.getText()));
    }

    public static void addJustHighLightLinks(DMBean bean) {
        bean.setListViewSpannableString(ListViewTool.convertNormalStringToSpannableString(bean.getText()));
    }


    public static boolean haveFilterWord(MessageBean content, List<String> filterWordList) {

        if (content.getUser().getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
            return false;
        }

        for (String filterWord : filterWordList) {

            if (content.getUser() != null && content.getUser().getScreen_name().contains(filterWord)) {
                return true;
            }

            if (content.getText().contains(filterWord)) {
                return true;
            }

            if (content.getRetweeted_status() != null && content.getRetweeted_status().getText().contains(filterWord)) {
                return true;
            }

            if (content.getRetweeted_status() != null && content.getRetweeted_status().getUser() != null
                    && content.getRetweeted_status().getUser().getScreen_name().contains(filterWord)) {
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
