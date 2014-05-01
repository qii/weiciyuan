package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.Utility;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.io.File;

/**
 * User: qii
 * Date: 14-4-30
 */
public class LargePictureFragment extends Fragment {

    private static final int NAVIGATION_BAR_HEIGHT_DP_UNIT = 48;

    public static LargePictureFragment newInstance(String path) {
        LargePictureFragment fragment = new LargePictureFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_large_layout, container, false);

        WebView large = (WebView) view.findViewById(R.id.large);
        large.setBackgroundColor(getResources().getColor(R.color.transparent));
        large.setVisibility(View.INVISIBLE);
        large.setOverScrollMode(View.OVER_SCROLL_NEVER);
        if (Utility.doThisDeviceOwnNavigationBar(getActivity())) {
            large.setPadding(0, 0, 0,
                    Utility.dip2px(NAVIGATION_BAR_HEIGHT_DP_UNIT));
        }

        if (SettingUtility.allowClickToCloseGallery()) {
            large.setOnTouchListener(largeOnTouchListener);
        }

        LongClickListener longClickListener = ((ContainerFragment) getParentFragment())
                .getLongClickListener();
        large.setOnLongClickListener(longClickListener);

        String path = getArguments().getString("path");

        large.getSettings().setJavaScriptEnabled(true);
        large.getSettings().setUseWideViewPort(true);
        large.getSettings().setLoadWithOverviewMode(true);
        large.getSettings().setBuiltInZoomControls(true);
        large.getSettings().setDisplayZoomControls(false);

        large.setVerticalScrollBarEnabled(false);
        large.setHorizontalScrollBarEnabled(false);

        File file = new File(path);

        String str1 = "file://" + file.getAbsolutePath().replace("/mnt/sdcard/", "/sdcard/");
        String str2 =
                "<html>\n<head>\n     <style>\n          html,body{background:transparent;margin:0;padding:0;}          *{-webkit-tap-highlight-color:rgba(0, 0, 0, 0);}\n     </style>\n     <script type=\"text/javascript\">\n     var imgUrl = \""
                        + str1 + "\";" + "     var objImage = new Image();\n"
                        + "     var realWidth = 0;\n" + "     var realHeight = 0;\n" + "\n"
                        + "     function onLoad() {\n"
                        + "          objImage.onload = function() {\n"
                        + "               realWidth = objImage.width;\n"
                        + "               realHeight = objImage.height;\n" + "\n"
                        + "               document.gagImg.src = imgUrl;\n"
                        + "               onResize();\n" + "          }\n"
                        + "          objImage.src = imgUrl;\n" + "     }\n" + "\n"
                        + "     function onResize() {\n" + "          var scale = 1;\n"
                        + "          var newWidth = document.gagImg.width;\n"
                        + "          if (realWidth > newWidth) {\n"
                        + "               scale = realWidth / newWidth;\n" + "          } else {\n"
                        + "               scale = newWidth / realWidth;\n" + "          }\n" + "\n"
                        + "          hiddenHeight = Math.ceil(30 * scale);\n"
                        + "          document.getElementById('hiddenBar').style.height = hiddenHeight + \"px\";\n"
                        + "          document.getElementById('hiddenBar').style.marginTop = -hiddenHeight + \"px\";\n"
                        + "     }\n" + "     </script>\n" + "</head>\n"
                        + "<body onload=\"onLoad()\" onresize=\"onResize()\" onclick=\"Android.toggleOverlayDisplay();\">\n"
                        + "     <table style=\"width: 100%;height:100%;\">\n"
                        + "          <tr style=\"width: 100%;\">\n"
                        + "               <td valign=\"middle\" align=\"center\" style=\"width: 100%;\">\n"
                        + "                    <div style=\"display:block\">\n"
                        + "                         <img name=\"gagImg\" src=\"\" width=\"100%\" style=\"\" />\n"
                        + "                    </div>\n"
                        + "                    <div id=\"hiddenBar\" style=\"position:absolute; width: 100%; background: transparent;\"></div>\n"
                        + "               </td>\n" + "          </tr>\n" + "     </table>\n"
                        + "</body>\n" + "</html>";
        large.loadDataWithBaseURL("file:///android_asset/", str2, "text/html", "utf-8", null);
        large.setVisibility(View.VISIBLE);

        return view;
    }


    private View.OnTouchListener largeOnTouchListener = new View.OnTouchListener() {
        boolean mPressed;

        boolean mClose;

        CheckForSinglePress mPendingCheckForSinglePress;

        long lastTime = 0;

        float[] location = new float[2];

        class CheckForSinglePress implements Runnable {

            View view;

            public CheckForSinglePress(View view) {
                this.view = view;
            }

            public void run() {
                if (!mPressed && mClose) {
                    Utility.playClickSound(view);
                    getActivity().onBackPressed();
                }
            }

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mPendingCheckForSinglePress = new CheckForSinglePress(v);
                    mPressed = true;
                    if (System.currentTimeMillis() - lastTime
                            > ViewConfiguration.getDoubleTapTimeout() + 100) {
                        mClose = true;
                        new Handler().postDelayed(mPendingCheckForSinglePress,
                                ViewConfiguration.getDoubleTapTimeout() + 100);
                    } else {
                        mClose = false;
                    }
                    lastTime = System.currentTimeMillis();

                    location[0] = event.getRawX();
                    location[1] = event.getRawY();

                    break;
                case MotionEvent.ACTION_UP:
                    mPressed = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mClose = false;

                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX();
                    float y = event.getRawY();
                    if (Math.abs(location[0] - x) > 5.0f && Math.abs(location[1] - y) > 5.0f) {
                        mClose = false;
                    }
                    break;
            }

            return false;
        }
    };

}
