package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.Utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.io.File;

/**
 * User: qii
 * Date: 14-4-30
 */
public class LargePictureFragment extends Fragment {

    private static final int NAVIGATION_BAR_HEIGHT_DP_UNIT = 48;

    public static LargePictureFragment newInstance(String path, boolean animationIn) {
        LargePictureFragment fragment = new LargePictureFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        bundle.putBoolean("animationIn", animationIn);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_large_layout, container, false);

        final WebView large = (WebView) view.findViewById(R.id.large);
        large.setBackgroundColor(getResources().getColor(R.color.transparent));
        large.setVisibility(View.INVISIBLE);
        large.setOverScrollMode(View.OVER_SCROLL_NEVER);

        if (SettingUtility.allowClickToCloseGallery()) {
            large.setOnTouchListener(new LargeOnTouchListener(large));
        }

        LongClickListener longClickListener = ((ContainerFragment) getParentFragment())
                .getLongClickListener();
        large.setOnLongClickListener(longClickListener);

        final String path = getArguments().getString("path");

        large.getSettings().setJavaScriptEnabled(true);
        large.getSettings().setUseWideViewPort(true);
        large.getSettings().setLoadWithOverviewMode(true);
        large.getSettings().setBuiltInZoomControls(true);
        large.getSettings().setDisplayZoomControls(false);

        large.setVerticalScrollBarEnabled(false);
        large.setHorizontalScrollBarEnabled(false);
        large.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        boolean animateIn = getArguments().getBoolean("animationIn");

        if (animateIn) {
            showContent(path, large);
        } else {
            /**
             * webview will influence other imageview animation performance
             */
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    showContent(path, large);
                }
            }, GeneralPictureFragment.ANIMATION_DURATION + 300);
        }

        return view;
    }

    private void showContent(String path, WebView large) {
        File file = new File(path);

        String str1 = "file://" + file.getAbsolutePath()
                .replace("/mnt/sdcard/", "/sdcard/");
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
                        + "               scale = realWidth / newWidth;\n"
                        + "          } else {\n"
                        + "               scale = newWidth / realWidth;\n" + "          }\n"
                        + "\n"
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
        large.loadDataWithBaseURL("file:///android_asset/", str2, "text/html", "utf-8",
                null);
        large.setVisibility(View.VISIBLE);
    }

    private class LargeOnTouchListener implements View.OnTouchListener {

        GestureDetector gestureDetector;

        public LargeOnTouchListener(final View view) {
            gestureDetector = new GestureDetector(view.getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            Utility.playClickSound(view);
                            getActivity().onBackPressed();
                            return true;
                        }
                    });
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return false;
        }
    }
}
