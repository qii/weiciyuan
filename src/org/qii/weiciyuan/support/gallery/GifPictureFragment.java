package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * User: qii
 * Date: 14-4-30
 */
public class GifPictureFragment extends Fragment {

    private static final int NAVIGATION_BAR_HEIGHT_DP_UNIT = 48;

    public static GifPictureFragment newInstance(String path) {
        GifPictureFragment fragment = new GifPictureFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_gif_layout, container, false);

        PhotoView gifImageView = (PhotoView) view.findViewById(R.id.animation);

        if (SettingUtility.allowClickToCloseGallery()) {
            gifImageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    getActivity().onBackPressed();

                }

            });
        }

        LongClickListener longClickListener = ((ContainerFragment) getParentFragment())
                .getLongClickListener();
        gifImageView.setOnLongClickListener(longClickListener);

        String path = getArguments().getString("path");

        File gifFile = new File(path);
        try {
            GifDrawable gifFromFile = new GifDrawable(gifFile);
            gifImageView.setImageDrawable(gifFromFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view;
    }

}
