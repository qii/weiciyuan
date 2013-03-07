package org.qii.weiciyuan.support.utils;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.widget.TextView;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.support.lib.MyLinkify;
import org.qii.weiciyuan.support.lib.WeiboPatterns;

import java.util.List;
import java.util.regex.Matcher;

/**
 * User: qii
 * Date: 12-8-29
 */
public class ListViewTool {

    private ListViewTool() {
    }


    public static void addLinks(TextView view) {

        MyLinkify.addLinks(view, WeiboPatterns.MENTION_URL, WeiboPatterns.MENTION_SCHEME);
        MyLinkify.addLinks(view, WeiboPatterns.TOPIC_URL, WeiboPatterns.TOPIC_SCHEME);
        MyLinkify.addLinks(view, WeiboPatterns.WEB_URL, WeiboPatterns.WEB_SCHEME);

        CharSequence content = view.getText();
        SpannableString value = SpannableString.valueOf(content);
        ListViewTool.addEmotions(value);
        view.setText(value);

    }

    public static SpannableString getJustHighLightLinks(String txt) {
        //hack to fix android imagespan bug,see http://stackoverflow.com/questions/3253148/imagespan-is-cut-off-incorrectly-aligned
        //if string only contains emotion tags,add a empty char to the end
        String hackTxt;
        if (txt.startsWith("[") && txt.endsWith("]")) {
            hackTxt = txt + " ";
        } else {
            hackTxt = txt;
        }
        SpannableString value;
        value = MyLinkify.getJustHighLightLinks(hackTxt, WeiboPatterns.MENTION_URL, WeiboPatterns.MENTION_SCHEME);
        value = MyLinkify.addJUstHighLightLinks(value, MyLinkify.WEB_URLS);
        value = MyLinkify.getJustHighLightLinks(value, WeiboPatterns.TOPIC_URL, WeiboPatterns.TOPIC_SCHEME);
        ListViewTool.addEmotions(value);
        return value;
    }

    public static void addJustHighLightLinks(MessageBean bean) {

        bean.setListViewSpannableString(ListViewTool.getJustHighLightLinks(bean.getText()));
        if (bean.getRetweeted_status() != null) {
            String name = "";
            UserBean reUser = bean.getRetweeted_status().getUser();
            if (reUser != null) {
                name = reUser.getScreen_name();
            }

            SpannableString value;

            if (!TextUtils.isEmpty(name)) {
                value = ListViewTool.getJustHighLightLinks("@" + name + "：" + bean.getRetweeted_status().getText());
            } else {
                value = ListViewTool.getJustHighLightLinks(bean.getRetweeted_status().getText());
            }

            bean.getRetweeted_status().setListViewSpannableString(value);
        }
    }

    public static void addJustHighLightLinks(CommentBean bean) {

        bean.setListViewSpannableString(ListViewTool.getJustHighLightLinks(bean.getText()));
        if (bean.getStatus() != null) {
            String name = "";
            UserBean reUser = bean.getStatus().getUser();
            if (reUser != null) {
                name = reUser.getScreen_name();
            }

            SpannableString value;

            if (!TextUtils.isEmpty(name)) {
                value = ListViewTool.getJustHighLightLinks("@" + name + "：" + bean.getStatus().getText());
            } else {
                value = ListViewTool.getJustHighLightLinks(bean.getStatus().getText());
            }

            bean.getStatus().setListViewSpannableString(value);
        }
    }

    public static void addJustHighLightLinksOnlyReplyComment(CommentBean bean) {


        String name = "";
        UserBean reUser = bean.getUser();
        if (reUser != null) {
            name = reUser.getScreen_name();
        }

        SpannableString value;

        if (!TextUtils.isEmpty(name)) {
            value = ListViewTool.getJustHighLightLinks("@" + name + "：" + bean.getText());
        } else {
            value = ListViewTool.getJustHighLightLinks(bean.getText());
        }

        bean.setListViewReplySpannableString(value);
    }

    public static void addJustHighLightLinks(DMUserBean bean) {
        bean.setListViewSpannableString(ListViewTool.getJustHighLightLinks(bean.getText()));
    }

    public static void addJustHighLightLinks(DMBean bean) {
        bean.setListViewSpannableString(ListViewTool.getJustHighLightLinks(bean.getText()));
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


    public static void addEmotions(SpannableString value) {
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
