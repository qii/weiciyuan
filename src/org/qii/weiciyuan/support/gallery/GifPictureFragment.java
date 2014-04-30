package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * User: qii
 * Date: 14-4-30
 */
public class GifPictureFragment extends Fragment {

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

        GifImageView gifImageView = (GifImageView) view.findViewById(R.id.animation);

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
