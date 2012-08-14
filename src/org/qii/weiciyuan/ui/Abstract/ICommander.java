package org.qii.weiciyuan.ui.Abstract;

import android.widget.ImageView;
import android.widget.ListView;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-8-9
 * Time: 下午8:28
 * To change this template use File | Settings | File Templates.
 */
public interface ICommander {


    public void downloadAvatar(ImageView view, String url, int position, ListView listView);

    public void downContentPic(ImageView view, String url, int position, ListView listView);
}