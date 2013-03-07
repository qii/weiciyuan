package org.qii.weiciyuan.support.lib;

import java.util.regex.Pattern;

/**
 * User: qii
 * Date: 12-12-27
 * fix web url bug which include chinese words, see WEB_URL
 */
public class WeiboPatterns {

    public static final Pattern WEB_URL = Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]");

    public static final Pattern TOPIC_URL = Pattern.compile("#([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)#");

    public static final Pattern MENTION_URL = Pattern.compile("@([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)");

    public static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");
}
