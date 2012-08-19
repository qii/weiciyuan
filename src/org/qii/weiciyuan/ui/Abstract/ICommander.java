package org.qii.weiciyuan.ui.Abstract;

import android.widget.ImageView;
import android.widget.ListView;

public interface ICommander {


    public void downloadAvatar(ImageView view, String url, int position, ListView listView);

    public void downContentPic(ImageView view, String url, int position, ListView listView);
}