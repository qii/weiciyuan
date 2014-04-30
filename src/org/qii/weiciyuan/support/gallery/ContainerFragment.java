package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.asyncdrawable.TaskCache;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.AnimationRect;
import org.qii.weiciyuan.support.lib.CircleProgressView;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

/**
 * User: qii
 * Date: 14-4-30
 */
public class ContainerFragment extends Fragment {

    private static HashMap<String, PicSimpleBitmapWorkerTask> taskMap
            = new HashMap<String, PicSimpleBitmapWorkerTask>();


    public static ContainerFragment newInstance(String url, AnimationRect rect,
            boolean animationIn, boolean firstOpenPage) {
        ContainerFragment fragment = new ContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putParcelable("rect", rect);
        bundle.putBoolean("animationIn", animationIn);
        bundle.putBoolean("firstOpenPage", firstOpenPage);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_container_layout, container, false);
        CircleProgressView progressView = (CircleProgressView) view.findViewById(R.id.loading);
        TextView wait = (TextView) view.findViewById(R.id.wait);
        TextView error = (TextView) view.findViewById(R.id.error);

        String url = getArguments().getString("url");
        boolean animateIn = getArguments().getBoolean("animationIn");

        String path = FileManager.getFilePathFromUrl(url, FileLocationMethod.picture_large);

        if (ImageUtility.isThisBitmapCanRead(path)) {
            displayPicture(path, animateIn);
        } else {
            GalleryAnimationActivity activity = (GalleryAnimationActivity) getActivity();
            activity.showBackgroundImmediately();
            PicSimpleBitmapWorkerTask task = new PicSimpleBitmapWorkerTask(progressView, wait,
                    error, url, taskMap);
            task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }

        return view;
    }


    private void displayPicture(String path, boolean animateIn) {

        GalleryAnimationActivity activity = (GalleryAnimationActivity) getActivity();

        AnimationRect rect = getArguments().getParcelable("rect");
        boolean firstOpenPage = getArguments().getBoolean("firstOpenPage");

        if (firstOpenPage) {
            if (animateIn) {
                ObjectAnimator animator = activity.showBackgroundAnimate();
                animator.start();
            } else {
                activity.showBackgroundImmediately();
            }
        }

        if (!ImageUtility.isThisBitmapTooLargeToRead(path)) {
            Fragment fragment = null;
            if (ImageUtility.isThisPictureGif(path)) {
                fragment = GifPictureFragment.newInstance(path);
            } else {
                fragment = GeneralPictureFragment.newInstance(path, rect, animateIn);
            }
            getChildFragmentManager().beginTransaction().replace(R.id.child, fragment).commit();
        } else {
            LargePictureFragment fragment = LargePictureFragment.newInstance(path);
            getChildFragmentManager().beginTransaction().replace(R.id.child, fragment).commit();
        }

    }


    public void animationExit(ObjectAnimator backgroundAnimator) {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child);
        if (fragment instanceof GeneralPictureFragment) {
            GeneralPictureFragment child = (GeneralPictureFragment) fragment;
            child.animationExit(backgroundAnimator);
        }
    }


    public boolean canAnimateCloseActivity() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child);
        if (fragment instanceof GeneralPictureFragment) {
            return true;
        } else {
            return false;
        }
    }

    private class PicSimpleBitmapWorkerTask extends MyAsyncTask<String, Integer, String> {

        private FileDownloaderHttpHelper.DownloadListener downloadListener
                = new FileDownloaderHttpHelper.DownloadListener() {
            @Override
            public void pushProgress(int progress, int max) {
                publishProgress(progress, max);
            }


        };

        public void setWidget(CircleProgressView spinner, TextView wait,
                TextView readError) {
            this.spinner = spinner;
            this.wait = wait;
            this.readError = readError;
        }


        private TextView wait;

        private String url;

        private CircleProgressView spinner;

        private TextView readError;

        private HashMap<String, PicSimpleBitmapWorkerTask> taskMap;

        public PicSimpleBitmapWorkerTask(CircleProgressView spinner, TextView wait,
                TextView readError, String url,
                HashMap<String, PicSimpleBitmapWorkerTask> taskMap) {
            this.url = url;
            this.spinner = spinner;
            this.readError = readError;
            this.taskMap = taskMap;
            this.wait = wait;
            this.readError.setVisibility(View.INVISIBLE);
            this.spinner.setVisibility(View.VISIBLE);

        }


        @Override
        protected String doInBackground(String... dd) {
            if (isCancelled()) {
                return null;
            }

            boolean downloaded = TaskCache.waitForMsgDetailPictureDownload(url, downloadListener);
            if (downloaded) {
                return FileManager.getFilePathFromUrl(url, FileLocationMethod.picture_large);
            } else {
                return null;
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            this.wait.setVisibility(View.INVISIBLE);
            int progress = values[0];
            int max = values[1];
            spinner.setMax(max);
            spinner.setProgress(progress);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            taskMap.remove(url);
            this.spinner.setVisibility(View.INVISIBLE);
            this.wait.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPostExecute(final String bitmapPath) {

            this.spinner.setVisibility(View.INVISIBLE);
            this.wait.setVisibility(View.INVISIBLE);

            if (isCancelled()) {
                return;
            }

            taskMap.remove(url);

            if (TextUtils.isEmpty(bitmapPath)) {

                readError.setVisibility(View.VISIBLE);
                readError.setText(getString(R.string.picture_cant_download_or_sd_cant_read));
                return;
            } else {
                readError.setVisibility(View.INVISIBLE);
            }

            if (!ImageUtility.isThisBitmapCanRead(bitmapPath)) {
                Toast.makeText(getActivity(),
                        R.string.download_finished_but_cant_read_picture_file, Toast.LENGTH_SHORT)
                        .show();
            }

            displayPicture(bitmapPath, false);


        }
    }

}
