package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.Utility;

import java.io.File;

/**
 * User: qii
 * Date: 13-7-15
 */
public class WeiboDetailImageView extends FrameLayout {

    protected ImageView mImageView;
    private WebView webView;
    private ProgressBar pb;
    private Button retry;

    public WeiboDetailImageView(Context context) {
        super(context);
    }

    public WeiboDetailImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeiboDetailImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.weibodetailimageview_layout, this, true);
        mImageView = (ImageView) v.findViewById(R.id.imageview);
        mImageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        webView = (WebView) v.findViewById(R.id.gif);

        pb = (ProgressBar) v.findViewById(R.id.imageview_pb);
        retry = (Button) v.findViewById(R.id.retry);

    }


    public void setImageDrawable(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bm) {
        mImageView.setImageBitmap(bm);
    }


    public ImageView getImageView() {
        return mImageView;
    }


    public void setProgress(int value, int max) {
        pb.setVisibility(View.VISIBLE);
        pb.setMax(max);
        pb.setProgress(value);
    }

    public ProgressBar getProgressBar() {
        return pb;
    }

    public Button getRetryButton() {
        return retry;
    }

    public void setGif(String bitmapPath) {
        webView.setVisibility(View.VISIBLE);


        if (webView.getTag() != null)
            return;

        webView.setBackgroundColor(getResources().getColor(R.color.transparent));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(false);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bitmapPath, options);

        int width = Math.max(Utility.dip2px(200), options.outWidth);
        int height = width * options.outHeight / options.outWidth;

        ViewGroup.LayoutParams layoutParams = webView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        webView.setLayoutParams(layoutParams);

        File file = new File(bitmapPath);
        String str1 = "file://" + file.getAbsolutePath().replace("/mnt/sdcard/", "/sdcard/");
        String str2 = "<html>\n<head>\n     <style>\n          html,body{background:transparent;margin:0;padding:0;}          *{-webkit-tap-highlight-color:rgba(0, 0, 0, 0);}\n     </style>\n     <script type=\"text/javascript\">\n     var imgUrl = \"" + str1 + "\";" + "     var objImage = new Image();\n" + "     var realWidth = 0;\n" + "     var realHeight = 0;\n" + "\n" + "     function onLoad() {\n" + "          objImage.onload = function() {\n" + "               realWidth = objImage.width;\n" + "               realHeight = objImage.height;\n" + "\n" + "               document.gagImg.src = imgUrl;\n" + "               onResize();\n" + "          }\n" + "          objImage.src = imgUrl;\n" + "     }\n" + "\n" + "     function onResize() {\n" + "          var scale = 1;\n" + "          var newWidth = document.gagImg.width;\n" + "          if (realWidth > newWidth) {\n" + "               scale = realWidth / newWidth;\n" + "          } else {\n" + "               scale = newWidth / realWidth;\n" + "          }\n" + "\n" + "          hiddenHeight = Math.ceil(30 * scale);\n" + "          document.getElementById('hiddenBar').style.height = hiddenHeight + \"px\";\n" + "          document.getElementById('hiddenBar').style.marginTop = -hiddenHeight + \"px\";\n" + "     }\n" + "     </script>\n" + "</head>\n" + "<body onload=\"onLoad()\" onresize=\"onResize()\" onclick=\"Android.toggleOverlayDisplay();\">\n" + "     <table style=\"width: 100%;height:100%;\">\n" + "          <tr style=\"width: 100%;\">\n" + "               <td valign=\"middle\" align=\"center\" style=\"width: 100%;\">\n" + "                    <div style=\"display:block\">\n" + "                         <img name=\"gagImg\" src=\"\" width=\"100%\" style=\"\" />\n" + "                    </div>\n" + "                    <div id=\"hiddenBar\" style=\"position:absolute; width: 100%; background: transparent;\"></div>\n" + "               </td>\n" + "          </tr>\n" + "     </table>\n" + "</body>\n" + "</html>";
        webView.loadDataWithBaseURL("file:///android_asset/", str2, "text/html", "utf-8", null);

        webView.setTag(new Object());
    }
}



