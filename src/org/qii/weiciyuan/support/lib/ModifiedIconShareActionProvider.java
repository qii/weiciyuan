package org.qii.weiciyuan.support.lib;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import org.qii.weiciyuan.support.utils.ThemeUtility;

/**
 * User: qii
 * Date: 13-9-29
 * issue: #150
 * android:actionModeShareDrawalbe is not a public attribute, and ShareActionProvider don't have any method to modify icon too.
 * so must override ShareActionProvider, find the first ImageView, set resource, this is just a workaround, a better solution is to
 * write custom ActionProvider
 */
public class ModifiedIconShareActionProvider extends ShareActionProvider {
    /**
     * Creates a new instance.
     *
     * @param context Context for accessing resources.
     */
    public ModifiedIconShareActionProvider(Context context) {
        super(context);
    }

    @Override
    public View onCreateActionView() {
        View view = super.onCreateActionView();
        setIcon((ViewGroup) view);
        return view;
    }

    private int index = 0;

    private void setIcon(ViewGroup viewGroup) {
        if (index > 1)
            return;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (child instanceof ViewGroup) {
                setIcon((ViewGroup) child);
            } else if (child instanceof ImageView) {
                index++;
                if (index == 1) {
                    ImageView iv = (ImageView) child;
                    iv.setImageResource(ThemeUtility.getActionBarShareItemIcon());
                }
            }


        }

    }
}
