package org.qii.weiciyuan.ui.userinfo;

import org.qii.weiciyuan.R;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;

/**
 * User: qii
 * Date: 12-12-9
 */
public class UserAvatarDialog extends DialogFragment {

    public static UserAvatarDialog newInstance(String path, Rect rect) {
        UserAvatarDialog dialog = new UserAvatarDialog();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        bundle.putParcelable("rect", rect);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String path = getArguments().getString("path");
        final Rect ori = getArguments().getParcelable("rect");

        Bitmap bitmap = BitmapFactory.decodeFile(path);

        final View content = getActivity().getLayoutInflater()
                .inflate(R.layout.useravatardialog_layout, null);

        final ImageView avatar = ((ImageView) content.findViewById(R.id.imageview));
        avatar.setImageBitmap(bitmap);
        avatar.setClickable(true);
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateClose(avatar, ori);
            }
        });

        Dialog dialog = new Dialog(getActivity(), R.style.UserAvatarDialog) {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

                    animateClose(avatar, ori);

                    return true;
                }
                return super.onKeyDown(keyCode, event);
            }
        };

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(content);

        content.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        content.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        if (ori == null) {
                            return;
                        }

                        int[] avatarLocation = new int[2];
                        avatar.getLocationOnScreen(avatarLocation);

                        final int transX = ori.left - avatarLocation[0];
                        final int transY = ori.top - avatarLocation[1];

                        final float scaleX = (float) ori.width() / (float) avatar.getWidth();
                        final float scaleY = (float) ori.height() / (float) avatar.getHeight();

                        avatar.setTranslationX(transX);
                        avatar.setTranslationY(transY);

                        avatar.setPivotX(0);
                        avatar.setPivotY(0);

                        avatar.setScaleX(scaleX);
                        avatar.setScaleY(scaleY);

                        avatar.animate().translationX(0).translationY(0).scaleY(1)
                                .scaleX(1).alpha(1.0f).setDuration(300)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                    }
                });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    private void animateClose(ImageView avatar, Rect ori) {
        if (ori == null) {
            return;
        }

        int[] avatarLocation = new int[2];
        avatar.getLocationOnScreen(avatarLocation);

        final int transX = ori.left - avatarLocation[0];
        final int transY = ori.top - avatarLocation[1];

        final float scaleX = (float) ori.width() / (float) avatar.getWidth();
        final float scaleY = (float) ori.height() / (float) avatar.getHeight();

        avatar.animate().translationX(transX).translationY(transY).scaleY(scaleY)
                .scaleX(scaleX).alpha(0.7f).rotationY(0f).setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        dismissAllowingStateLoss();
                    }
                });
    }
}
