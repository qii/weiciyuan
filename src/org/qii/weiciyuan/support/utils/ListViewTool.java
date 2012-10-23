package org.qii.weiciyuan.support.utils;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.widget.TextView;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.lib.MyLinkify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
        //hack to fix android imagespan bug,see http://stackoverflow.com/questions/3253148/imagespan-is-cut-off-incorrectly-aligned
        //if string only contains emotion tags,add a empty char to the end
        String hackTxt;
        if (txt.startsWith("[") && txt.endsWith("]")) {
            hackTxt = txt + " ";
        } else {
            hackTxt = txt;
        }
        SpannableString value;
        MyLinkify.TransformFilter mentionFilter = new MyLinkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return match.group(1);
            }
        };

        // Match @mentions and capture just the username portion of the text.
        Pattern pattern = Pattern.compile("@([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)");
        String scheme = "org.qii.weiciyuan://";
        value = MyLinkify.getJustHighLightLinks(hackTxt, pattern, scheme, null, mentionFilter);

        value = MyLinkify.addJUstHighLightLinks(value, MyLinkify.WEB_URLS);


        Pattern dd = Pattern.compile("#([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)#");
        value = MyLinkify.getJustHighLightLinks(value, dd, scheme, null, mentionFilter);

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


        pattern = Pattern.compile("#([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)#");
        scheme = "org.qii.weiciyuan.topic://";
        MyLinkify.addLinks(view, pattern, scheme, null, mentionFilter);

        MyLinkify.addLinks(view, MyLinkify.WEB_URLS);
        CharSequence content = view.getText();
        SpannableString value = SpannableString.valueOf(content);
        ListViewTool.addEmotions(value, view.getTextSize());
        view.setText(value);

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
        Matcher localMatcher = Pattern.compile("\\[(\\S+?)\\]").matcher(value);
        while (localMatcher.find()) {
            String str2 = localMatcher.group(0);

            int k = localMatcher.start();
            int m = localMatcher.end();
            if (m - k < 8) {

                String url = GlobalContext.getInstance().getEmotions().get(str2);
                if (!TextUtils.isEmpty(url)) {
                    String path = FileManager.getFilePathFromUrl(url, FileLocationMethod.emotion);
                    String name = new File(path).getName();
                    AssetManager assetManager = GlobalContext.getInstance().getAssets();
                    InputStream inputStream;
                    try {
                        inputStream = assetManager.open(name);

                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            ImageSpan localImageSpan = new ImageSpan(GlobalContext.getInstance().getActivity(), bitmap, ImageSpan.ALIGN_BASELINE);
                            value.setSpan(localImageSpan, k, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } catch (IOException e) {
                        AppLogger.e(e.getMessage());
                    }
                }
            }
        }
    }

    public static void addEmotions(SpannableString value, float height) {
        Matcher localMatcher = Pattern.compile("\\[(\\S+?)\\]").matcher(value);
        while (localMatcher.find()) {
            String str2 = localMatcher.group(0);

            int k = localMatcher.start();
            int m = localMatcher.end();
            if (m - k < 8) {

                String url = GlobalContext.getInstance().getEmotions().get(str2);
                if (!TextUtils.isEmpty(url)) {
                    String path = FileManager.getFilePathFromUrl(url, FileLocationMethod.emotion);
                    String name = new File(path).getName();
                    AssetManager assetManager = GlobalContext.getInstance().getAssets();
                    InputStream inputStream;
                    try {
                        inputStream = assetManager.open(name);

                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) (height * 1.5), (int) (height * 1.5), true);
                            bitmap.recycle();
                            ImageSpan localImageSpan = new ImageSpan(GlobalContext.getInstance().getActivity(), scaledBitmap, ImageSpan.ALIGN_BASELINE);
                            value.setSpan(localImageSpan, k, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } catch (IOException e) {
                        AppLogger.e(e.getMessage());
                    }

                }
            }
        }
    }


}
