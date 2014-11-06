package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.lib.AnimationRect;
import org.qii.weiciyuan.support.utils.AnimationUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: qii
 * Date: 14-3-21
 */
public class GalleryAnimationActivity extends FragmentActivity {

    private static final int STATUS_BAR_HEIGHT_DP_UNIT = 25;

    private ArrayList<AnimationRect> rectList;
    private ArrayList<String> urls = new ArrayList<String>();

    private ViewPager pager;
    private TextView position;
    private View background;

    private int initPosition;

    private ColorDrawable backgroundColor;

    public static Intent newIntent(MessageBean msg, ArrayList<AnimationRect> rectList,
            int initPosition) {
        Intent intent = new Intent(GlobalContext.getInstance(), GalleryAnimationActivity.class);
        intent.putExtra("msg", msg);
        intent.putExtra("rect", rectList);
        intent.putExtra("position", initPosition);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galleryactivity_animation_layout);

        rectList = getIntent().getParcelableArrayListExtra("rect");
        MessageBean msg = getIntent().getParcelableExtra("msg");
        ArrayList<String> tmp = msg.getThumbnailPicUrls();
        for (int i = 0; i < tmp.size(); i++) {
            urls.add(tmp.get(i).replace("thumbnail", "large"));
        }

        boolean disableHardwareLayerType = false;

        for (String url : urls) {
            if (url.contains(".gif")) {
                disableHardwareLayerType = true;
                break;
            }
        }

        position = (TextView) findViewById(R.id.position);
        initPosition = getIntent().getIntExtra("position", 0);

        pager = (ViewPager) findViewById(R.id.pager);

        pager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager()));
        final boolean finalDisableHardwareLayerType = disableHardwareLayerType;
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                GalleryAnimationActivity.this.position.setText(String.valueOf(position + 1));
            }

            @Override
            public void onPageScrollStateChanged(int scrollState) {
                if (scrollState != ViewPager.SCROLL_STATE_IDLE && finalDisableHardwareLayerType) {
                    final int childCount = pager.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = pager.getChildAt(i);
                        if (child.getLayerType() != View.LAYER_TYPE_NONE) {
                            child.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                    }
                }
            }
        });
        pager.setCurrentItem(getIntent().getIntExtra("position", 0));
        pager.setOffscreenPageLimit(1);
        pager.setPageTransformer(true, new ZoomOutPageTransformer());

        TextView sum = (TextView) findViewById(R.id.sum);
        sum.setText(String.valueOf(urls.size()));

        background = AnimationUtility.getAppContentView(this);

        if (savedInstanceState != null) {
            showBackgroundImmediately();
        }
    }

    private HashMap<Integer, ContainerFragment> fragmentMap
            = new HashMap<Integer, ContainerFragment>();

    private boolean alreadyAnimateIn = false;

    private class ImagePagerAdapter extends FragmentPagerAdapter {

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            ContainerFragment fragment = fragmentMap.get(position);
            if (fragment == null) {

                boolean animateIn = (initPosition == position) && !alreadyAnimateIn;
                fragment = ContainerFragment
                        .newInstance(urls.get(position), rectList.get(position), animateIn,
                                initPosition == position);
                alreadyAnimateIn = true;
                fragmentMap.put(position, fragment);
            }

            return fragment;
        }

        //when activity is recycled, ViewPager will reuse fragment by theirs name, so
        //getItem wont be called, but we need fragmentMap to animate close operation
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            if (object instanceof Fragment) {
                fragmentMap.put(position, (ContainerFragment) object);
            }
        }

        @Override
        public int getCount() {
            return urls.size();
        }
    }

    public void showBackgroundImmediately() {
        if (background.getBackground() == null) {
            backgroundColor = new ColorDrawable(Color.BLACK);
            background.setBackground(backgroundColor);
        }
    }

    public ObjectAnimator showBackgroundAnimate() {
        backgroundColor = new ColorDrawable(Color.BLACK);
        background.setBackground(backgroundColor);
        ObjectAnimator bgAnim = ObjectAnimator
                .ofInt(backgroundColor, "alpha", 0, 255);
        bgAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                background.setBackground(backgroundColor);
            }
        });
        return bgAnim;
    }

    @Override
    public void onBackPressed() {

        ContainerFragment fragment = fragmentMap.get(pager.getCurrentItem());
        if (fragment != null && fragment.canAnimateCloseActivity()) {
            backgroundColor = new ColorDrawable(Color.BLACK);
            ObjectAnimator bgAnim = ObjectAnimator.ofInt(backgroundColor, "alpha", 0);
            bgAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    background.setBackground(backgroundColor);
                }
            });
            bgAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    GalleryAnimationActivity.super.finish();
                    overridePendingTransition(-1, -1);
                }
            });
            fragment.animationExit(bgAnim);
        } else {
            super.onBackPressed();
        }
    }
}
