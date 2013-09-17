package org.qii.weiciyuan.support.settinghelper;

import android.content.Context;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.preference.SettingActivity;

/**
 * User: qii
 * Date: 12-11-28
 */
public class SettingUtility {

    private static final String FIRSTSTART = "firststart";
    private static final String LAST_FOUND_WEIBO_ACCOUNT_LINK = "last_found_weibo_account_link";

    private SettingUtility() {

    }

    public static void setDefaultAccountId(String id) {
        SettingHelper.setEditor(getContext(), "id", id);
    }

    public static String getDefaultAccountId() {
        return SettingHelper.getSharedPreferences(getContext(), "id", "");
    }

    private static Context getContext() {
        return GlobalContext.getInstance();
    }

    public static boolean firstStart() {
        boolean value = SettingHelper.getSharedPreferences(getContext(), FIRSTSTART, true);
        if (value)
            SettingHelper.setEditor(getContext(), FIRSTSTART, false);
        return value;
    }

    public static boolean isEnableFilter() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.FILTER, false);
    }


    public static int getFontSize() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.FONT_SIZE, "15");
        return Integer.valueOf(value);
    }

    public static int getAppTheme() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.THEME, "1");

        switch (Integer.valueOf(value)) {
            case 1:
                return R.style.AppTheme_Light;

            case 2:
                return R.style.AppTheme_Dark;

            default:
                return R.style.AppTheme_Light;

        }
    }

    public static void switchToAnotherTheme() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.THEME, "1");
        switch (Integer.valueOf(value)) {
            case 1:
                SettingHelper.setEditor(getContext(), SettingActivity.THEME, "2");
                break;
            case 2:
                SettingHelper.setEditor(getContext(), SettingActivity.THEME, "1");
                break;
            default:
                SettingHelper.setEditor(getContext(), SettingActivity.THEME, "1");
                break;

        }
    }

    public static int getHighPicMode() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.LIST_HIGH_PIC_MODE, "2");
        return Integer.valueOf(value);
    }


    public static int getCommentRepostAvatar() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.COMMENT_REPOST_AVATAR, "1");
        return Integer.valueOf(value);
    }

    public static int getListAvatarMode() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.LIST_AVATAR_MODE, "1");
        return Integer.valueOf(value);
    }

    public static int getListPicMode() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.LIST_PIC_MODE, "1");
        return Integer.valueOf(value);
    }


    public static void setEnableCommentRepostAvatar(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.SHOW_COMMENT_REPOST_AVATAR, value);
    }


    public static boolean getEnableCommentRepostListAvatar() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.SHOW_COMMENT_REPOST_AVATAR, true);
    }


    public static int getNotificationStyle() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.JBNOTIFICATION_STYLE, "1");

        switch (Integer.valueOf(value)) {
            case 1:
                return 1;

            case 2:
                return 2;

            default:
                return 1;

        }
    }


    public static boolean isEnablePic() {
        return !SettingHelper.getSharedPreferences(getContext(), SettingActivity.DISABLE_DOWNLOAD_AVATAR_PIC, false);
    }

    public static boolean getEnableBigPic() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.SHOW_BIG_PIC, false);
    }

    public static boolean getEnableFetchMSG() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_FETCH_MSG, false);
    }


    public static boolean getEnableAutoRefresh() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.AUTO_REFRESH, false);
    }


    public static boolean getEnableBigAvatar() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.SHOW_BIG_AVATAR, false);
    }

    public static boolean getEnableSound() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.SOUND, true)
                && Utility.isSystemRinger(getContext());
    }

    public static boolean disableFetchAtNight() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.DISABLE_FETCH_AT_NIGHT, true)
                && Utility.isSystemRinger(getContext());
    }

    public static String getFrequency() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.FREQUENCY, "1");
    }

    public static void setEnableBigPic(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.SHOW_BIG_PIC, value);
    }


    public static void setEnableBigAvatar(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.SHOW_BIG_AVATAR, value);
    }

    public static void setEnableFilter(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.FILTER, value);
    }

    public static void setEnableFetchMSG(boolean value) {
        SettingHelper.setEditor(getContext(), SettingActivity.ENABLE_FETCH_MSG, value);
    }

    public static boolean allowVibrate() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_VIBRATE, false);

    }

    public static boolean allowLed() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_LED, false);

    }

    public static String getRingtone() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_RINGTONE, "");

    }

    public static boolean allowFastScroll() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.LIST_FAST_SCROLL, true);

    }


    public static boolean allowMentionToMe() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_MENTION_TO_ME, true);

    }


    public static boolean allowCommentToMe() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_COMMENT_TO_ME, true);

    }


    public static boolean allowMentionCommentToMe() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_MENTION_COMMENT_TO_ME, true);

    }

    public static String getMsgCount() {
        String value = SettingHelper.getSharedPreferences(getContext(), SettingActivity.MSG_COUNT, "3");

        switch (Integer.valueOf(value)) {
            case 1:
                return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_25);

            case 2:
                return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_50);

            case 3:
                if (Utility.isConnected(getContext())) {
                    if (Utility.isWifi(getContext()))
                        return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_50);
                    else
                        return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_25);
                }

        }
        return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_25);

    }

    public static boolean disableHardwareAccelerated() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.DISABLE_HARDWARE_ACCELERATED, false);

    }

    public static int getUploadQuality() {
        String result = SettingHelper.getSharedPreferences(getContext(), SettingActivity.UPLOAD_PIC_QUALITY, "2");
        return Integer.valueOf(result);
    }

    public static void setDefaultSoftKeyBoardHeight(int height) {
        SettingHelper.setEditor(getContext(), "default_softkeyboard_height", height);
    }

    public static int getDefaultSoftKeyBoardHeight() {
        return SettingHelper.getSharedPreferences(getContext(), "default_softkeyboard_height", 400);
    }

    public static String getLastFoundWeiboAccountLink() {
        return SettingHelper.getSharedPreferences(getContext(), LAST_FOUND_WEIBO_ACCOUNT_LINK, "");
    }

    public static void setLastFoundWeiboAccountLink(String url) {
        SettingHelper.setEditor(getContext(), LAST_FOUND_WEIBO_ACCOUNT_LINK, url);
    }

    public static boolean isReadStyleEqualWeibo() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.READ_STYLE, "1").equals("1");
    }

    public static boolean isWifiUnlimitedMsgCount() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.WIFI_UNLIMITED_MSG_COUNT, true);
    }

    public static boolean isWifiAutoDownloadPic() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.WIFI_AUTO_DOWNLOAD_PIC, true);
    }

    public static boolean allowInternalWebBrowser() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_INTERNAL_WEB_BROWSER, true);

    }

    public static boolean allowClickToCloseGallery() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.ENABLE_CLICK_TO_CLOSE_GALLERY, true);

    }
}
