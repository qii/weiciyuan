package org.qii.weiciyuan.support.utils;

import android.text.SpannableString;
import android.widget.TextView;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.lib.MyLinkify;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: qii
 * Date: 12-8-29
 */
public class ListViewTool {
    public static void addJustHighLightLinks(TextView view) {
        MyLinkify.TransformFilter mentionFilter = new MyLinkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return match.group(1);
            }
        };

        // Match @mentions and capture just the username portion of the text.
        Pattern pattern = Pattern.compile("@([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)");
        String scheme = "org.qii.weiciyuan://";
        MyLinkify.addJustHighLightLinks(view, pattern, scheme, null, mentionFilter);

        MyLinkify.addJUstHighLightLinks(view, MyLinkify.WEB_URLS);

        Pattern dd = Pattern.compile("#([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)#");
        MyLinkify.addJustHighLightLinks(view, dd, scheme, null, mentionFilter);
    }


    public static SpannableString getJustHighLightLinks(String txt) {

        SpannableString value;
        MyLinkify.TransformFilter mentionFilter = new MyLinkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return match.group(1);
            }
        };

        // Match @mentions and capture just the username portion of the text.
        Pattern pattern = Pattern.compile("@([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)");
        String scheme = "org.qii.weiciyuan://";
        value = MyLinkify.getJustHighLightLinks(txt, pattern, scheme, null, mentionFilter);


        Pattern dd = Pattern.compile("#([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)#");
        value = MyLinkify.getJustHighLightLinks(value, dd, scheme, null, mentionFilter);

        return value;
    }

    public static void addJustHighLightLinks(MessageBean bean) {

        bean.setSpannableString(ListViewTool.getJustHighLightLinks(bean.getText()));
        if (bean.getRetweeted_status() != null) {
            bean.getRetweeted_status().setSpannableString(ListViewTool.getJustHighLightLinks("@" + bean.getRetweeted_status().getUser().getScreen_name() + "：" + bean.getRetweeted_status().getText()));
        }
    }

    public static void addLinks(TextView view) {
        MyLinkify.TransformFilter mentionFilter = new MyLinkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return match.group(1);
            }
        };


//
//        // Match @mentions and capture just the username portion of the text.
        Pattern pattern = Pattern.compile("@([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)");
        String scheme = "org.qii.weiciyuan://";
        MyLinkify.addLinks(view, pattern, scheme, null, mentionFilter);
        MyLinkify.addLinks(view, MyLinkify.WEB_URLS);

    }
}
