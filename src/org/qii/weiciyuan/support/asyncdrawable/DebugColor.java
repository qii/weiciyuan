package org.qii.weiciyuan.support.asyncdrawable;

import org.qii.weiciyuan.support.utils.Utility;

import android.graphics.Color;

/**
 * User: qii
 * Date: 13-2-8
 */
public class DebugColor {

    //    public static int DOWNLOAD_START = Color.BLUE;
//    public static int DOWNLOAD_FAILED = Color.RED;
//    public static int DOWNLOAD_CANCEL = Color.BLACK;
//    public static int PICTURE_ERROR = Color.YELLOW;
//    public static int LISTVIEW_FLING = Color.GREEN;
    public static int DOWNLOAD_START = Color.TRANSPARENT;
    public static int CHOOSE_CANCEL = !Utility.isDebugMode() ? Color.TRANSPARENT : Color.BLACK;
    public static int DOWNLOAD_FAILED = !Utility.isDebugMode() ? Color.TRANSPARENT : Color.RED;
    public static int DOWNLOAD_CANCEL = !Utility.isDebugMode() ? Color.TRANSPARENT : Color.GREEN;
    public static int READ_FAILED = !Utility.isDebugMode() ? Color.TRANSPARENT : Color.BLUE;
    public static int READ_CANCEL = !Utility.isDebugMode() ? Color.TRANSPARENT : Color.YELLOW;
    public static int LISTVIEW_FLING = Color.TRANSPARENT;
}
