package org.qii.weiciyuan.support.smileypicker;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.SmileyPickerUtility;
import org.qii.weiciyuan.support.utils.Utility;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: qii
 * Date: 13-1-18
 */
public class SmileyPicker extends LinearLayout {

    private int mPickerHeight;

    private EditText mEditText;
    private ViewPager viewPager;
    private ImageView centerPoint;
    private ImageView leftPoint;
    private ImageView rightPoint;
    private LayoutInflater mInflater;

    private Activity activity;

    private final LayoutTransition transitioner = new LayoutTransition();

    public SmileyPicker(Context paramContext) {
        super(paramContext);
    }

    public SmileyPicker(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        this.mInflater = LayoutInflater.from(paramContext);
        View view = this.mInflater.inflate(R.layout.writeweiboactivity_smileypicker, null);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new SmileyPagerAdapter());
        leftPoint = (ImageView) view.findViewById(R.id.left_point);
        centerPoint = (ImageView) view.findViewById(R.id.center_point);
        rightPoint = (ImageView) view.findViewById(R.id.right_point);
        if (Utility.isKK()) {
            rightPoint.setVisibility(View.VISIBLE);
        } else {
            rightPoint.setVisibility(View.GONE);
        }
        leftPoint.getDrawable().setLevel(1);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        leftPoint.getDrawable().setLevel(1);
                        centerPoint.getDrawable().setLevel(0);
                        rightPoint.getDrawable().setLevel(0);
                        break;
                    case 1:
                        leftPoint.getDrawable().setLevel(0);
                        centerPoint.getDrawable().setLevel(1);
                        rightPoint.getDrawable().setLevel(0);
                        break;
                    case 2:
                        leftPoint.getDrawable().setLevel(0);
                        centerPoint.getDrawable().setLevel(0);
                        rightPoint.getDrawable().setLevel(1);
                        break;
                }
            }
        });
        addView(view);
    }

    public void setEditText(Activity activity, ViewGroup rootLayout, EditText paramEditText) {
        this.mEditText = paramEditText;
        this.activity = activity;
        rootLayout.setLayoutTransition(transitioner);
        setupAnimations(transitioner);
    }

    public void show(Activity paramActivity, boolean showAnimation) {
        if (showAnimation) {
            transitioner.setDuration(200);
        } else {
            transitioner.setDuration(0);
        }
        this.mPickerHeight = SmileyPickerUtility.getKeyboardHeight(paramActivity);
        SmileyPickerUtility.hideSoftInput(this.mEditText);
        getLayoutParams().height = this.mPickerHeight;
        setVisibility(View.VISIBLE);
        //open smilepicker, press home, press app switcher to return to write weibo interface,
        //softkeyboard will be opened by android system when smilepicker is showing,
        // this method is used to fix this issue
        paramActivity.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void hide(Activity paramActivity) {
        setVisibility(View.GONE);
        paramActivity.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private class SmileyPagerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = activity.getLayoutInflater()
                    .inflate(R.layout.smileypicker_gridview, container, false);

            GridView gridView = (GridView) view.findViewById(R.id.smiley_grid);

            gridView.setAdapter(new SmileyAdapter(activity, position));
            container.addView(view, 0,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));

            return view;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getCount() {
            return Utility.isKK() ? 3 : 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }
    }

    private final class SmileyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<String> keys;
        private Map<String, Bitmap> bitmapMap;
        private int emotionPosition;
        private int count;

        public SmileyAdapter(Context context, int emotionPosition) {
            this.emotionPosition = emotionPosition;
            this.mInflater = LayoutInflater.from(context);
            this.keys = new ArrayList<String>();
            Set<String> keySet;
            switch (emotionPosition) {
                case SmileyMap.GENERAL_EMOTION_POSITION:
                    keySet = GlobalContext.getInstance().getEmotionsPics().keySet();
                    keys.addAll(keySet);
                    bitmapMap = GlobalContext.getInstance().getEmotionsPics();
                    count = bitmapMap.size();
                    break;
                case SmileyMap.EMOJI_EMOTION_POSITION:
                    keySet = EmojiMap.getInstance().getMap().keySet();
                    keys.addAll(keySet);
                    bitmapMap = null;
                    count = keys.size();
                    break;
                case SmileyMap.HUAHUA_EMOTION_POSITION:
                    keySet = GlobalContext.getInstance().getHuahuaPics().keySet();
                    keys.addAll(keySet);
                    bitmapMap = GlobalContext.getInstance().getHuahuaPics();
                    count = bitmapMap.size();
                    break;
                default:
                    throw new IllegalArgumentException("emotion position is invalid");
            }
        }

        private void bindView(final int position, View contentView) {
            ImageView imageView = ((ImageView) contentView.findViewById(R.id.smiley_item));
            TextView textView = (TextView) contentView.findViewById(R.id.smiley_text_item);
            if (emotionPosition != SmileyMap.EMOJI_EMOTION_POSITION) {
                imageView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.INVISIBLE);
                imageView.setImageBitmap(bitmapMap.get(keys.get(position)));
            } else {
                imageView.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText(keys.get(position));
            }

            contentView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ori = mEditText.getText().toString();
                    int index = mEditText.getSelectionStart();
                    StringBuilder stringBuilder = new StringBuilder(ori);
                    stringBuilder.insert(index, keys.get(position));
                    mEditText.setText(stringBuilder.toString());
                    mEditText.setSelection(index + keys.get(position).length());
                }
            });
        }

        public int getCount() {
            return count;
        }

        public Object getItem(int paramInt) {
            return null;
        }

        public long getItemId(int paramInt) {
            return 0L;
        }

        public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
            if (paramView == null) {
                paramView = this.mInflater
                        .inflate(R.layout.writeweiboactivity_smileypicker_item, null);
            }
            bindView(paramInt, paramView);
            return paramView;
        }
    }

    private void setupAnimations(LayoutTransition transition) {
        ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "translationY",
                SmileyPickerUtility.getScreenHeight(this.activity), mPickerHeight).
                setDuration(transition.getDuration(LayoutTransition.APPEARING));
        transition.setAnimator(LayoutTransition.APPEARING, animIn);

        ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "translationY", mPickerHeight,
                SmileyPickerUtility.getScreenHeight(this.activity)).
                setDuration(transition.getDuration(LayoutTransition.DISAPPEARING));
        transition.setAnimator(LayoutTransition.DISAPPEARING, animOut);
    }
}