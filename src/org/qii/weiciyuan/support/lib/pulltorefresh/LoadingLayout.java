/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.qii.weiciyuan.support.lib.pulltorefresh;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;

public class LoadingLayout extends FrameLayout {

    private final ImageView mHeaderImage;

    private final TextView mHeaderText;
    private final TextView mSubHeaderText;

    private String mPullLabel;
    private String mRefreshingLabel;
    private String mReleaseLabel;

    private LinearLayout header_text;
    private LinearLayout imagelayout;

    Context context;

    public LoadingLayout(Context context, final PullToRefreshBase.Mode mode, TypedArray attrs) {
        super(context);
        this.context = context;
        ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header, this);
        mHeaderText = (TextView) header.findViewById(R.id.pull_to_refresh_text);
        mSubHeaderText = (TextView) header.findViewById(R.id.pull_to_refresh_sub_text);
        mHeaderImage = (ImageView) header.findViewById(R.id.pull_to_refresh_image);
        header_text = (LinearLayout) header.findViewById(R.id.header_text);
        imagelayout = (LinearLayout) header.findViewById(R.id.imagelayout);

        mHeaderImage.setScaleType(ScaleType.MATRIX);


        switch (mode) {
            case PULL_UP_TO_REFRESH:
                // Load in labels
                mPullLabel = context.getString(R.string.pull_to_refresh_from_bottom_pull_label);
                mRefreshingLabel = context.getString(R.string.pull_to_refresh_from_bottom_refreshing_label);
                mReleaseLabel = context.getString(R.string.pull_to_refresh_from_bottom_release_label);
                break;

            case PULL_DOWN_TO_REFRESH:
            default:
                // Load in labels
                mPullLabel = context.getString(R.string.pull_to_refresh_pull_label);
                mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
                mReleaseLabel = context.getString(R.string.pull_to_refresh_release_label);


                break;
        }

        if (attrs.hasValue(R.styleable.PullToRefreshListView_ptrHeaderTextColor)) {
            ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefreshListView_ptrHeaderTextColor);
            setTextColor(null != colors ? colors : ColorStateList.valueOf(0xFF000000));
        }
        if (attrs.hasValue(R.styleable.PullToRefreshListView_ptrHeaderSubTextColor)) {
            ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefreshListView_ptrHeaderSubTextColor);
            setSubTextColor(null != colors ? colors : ColorStateList.valueOf(0xFF000000));
        }
//        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
//            Drawable background = attrs.getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground);
//            if (null != background) {
//                setBackgroundDrawable(background);
//            }
//        }

        reset();
    }

    public void reset() {
        mHeaderText.setText(wrapHtmlLabel(mPullLabel));
        header_text.setVisibility(View.VISIBLE);
//        mHeaderImage.setVisibility(View.GONE);
        imagelayout.setVisibility(View.GONE);
        mHeaderImage.clearAnimation();


        if (TextUtils.isEmpty(mSubHeaderText.getText())) {
            mSubHeaderText.setVisibility(View.GONE);
        } else {
            mSubHeaderText.setVisibility(View.VISIBLE);
        }
    }

    public void releaseToRefresh() {
        mHeaderText.setText(wrapHtmlLabel(mReleaseLabel));
        if (SettingUtility.getEnableSound()) {
            final MediaPlayer mp = MediaPlayer.create(context, R.raw.psst1);
            mp.start();
        }
    }

    public void pullToRefresh() {
        mHeaderText.setText(wrapHtmlLabel(mPullLabel));
        if (SettingUtility.getEnableSound()) {
            final MediaPlayer mp = MediaPlayer.create(context, R.raw.pop);
            mp.start();
        }

    }

    public void setPullLabel(String pullLabel) {
        mPullLabel = pullLabel;
    }

    public void refreshing() {
        header_text.setVisibility(View.GONE);
        imagelayout.setVisibility(View.VISIBLE);
//        mHeaderImage.setVisibility(View.VISIBLE);
        mHeaderText.setText(wrapHtmlLabel(mRefreshingLabel));
        mHeaderImage.startAnimation(AnimationUtils.loadAnimation(context, R.anim.refresh));

        mSubHeaderText.setVisibility(View.GONE);
    }

    public void setRefreshingLabel(String refreshingLabel) {
        mRefreshingLabel = refreshingLabel;
    }

    public void setReleaseLabel(String releaseLabel) {
        mReleaseLabel = releaseLabel;
    }


    public void setTextColor(ColorStateList color) {
        mHeaderText.setTextColor(color);
        mSubHeaderText.setTextColor(color);
    }

    public void setSubTextColor(ColorStateList color) {
        mSubHeaderText.setTextColor(color);
    }

    public void setTextColor(int color) {
        setTextColor(ColorStateList.valueOf(color));
    }


    public void setSubTextColor(int color) {
        setSubTextColor(ColorStateList.valueOf(color));
    }

    public void setSubHeaderText(CharSequence label) {
        if (TextUtils.isEmpty(label)) {
            mSubHeaderText.setVisibility(View.GONE);
        } else {
            mSubHeaderText.setText(label);
            mSubHeaderText.setVisibility(View.VISIBLE);
        }
    }

    public void onPullY(float scaleOfHeight) {
//        mHeaderImageMatrix.setRotate(scaleOfHeight * 90, mRotationPivotX, mRotationPivotY);
//        mHeaderImage.setImageMatrix(mHeaderImageMatrix);
    }


    private CharSequence wrapHtmlLabel(String label) {
        if (!isInEditMode()) {
            return Html.fromHtml(label);
        } else {
            return label;
        }
    }
}
