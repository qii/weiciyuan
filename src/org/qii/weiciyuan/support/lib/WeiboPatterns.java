package org.qii.weiciyuan.support.lib;

import org.qii.weiciyuan.BuildConfig;

import java.util.regex.Pattern;

/**
 * User: qii
 * Date: 12-12-27
 *
 * AndroidManifest.xml Activity intent-filter
 */
public class WeiboPatterns {

    public static final Pattern WEB_URL = Pattern
            .compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]");
    public static final Pattern TOPIC_URL = Pattern
            .compile("#[\\p{Print}\\p{InCJKUnifiedIdeographs}&&[^#]]+#");
    public static final Pattern MENTION_URL = Pattern
            .compile("@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}");
    public static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");

    public static final String WEB_SCHEME = "http://";
    public static final String TOPIC_SCHEME = BuildConfig.APPLICATION_ID + ".topic://";
    public static final String MENTION_SCHEME = BuildConfig.APPLICATION_ID + ".mention://";
}
