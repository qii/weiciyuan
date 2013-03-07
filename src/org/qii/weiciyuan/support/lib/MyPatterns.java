package org.qii.weiciyuan.support.lib;

import java.util.regex.Pattern;

/**
 * User: qii
 * Date: 12-12-27
 * fix web url bug which include chinese words, see WEB_URL
 */
public class MyPatterns {
    public static final Pattern WEB_URL = Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]");
}
