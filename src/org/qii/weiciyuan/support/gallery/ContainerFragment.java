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
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        if (ImageUtility.isThisBitmapCanRead(path) && taskMap.get(url) == null
                && TaskCache.isThisUrlTaskFinished(url)) {
            displayPicture(path, animateIn);
        } else {
            GalleryAnimationActivity activity = (GalleryAnimationActivity) getActivity();
            activity.showBackgroundImmediately();
            progressView.setVisibility(View.VISIBLE);

            if (taskMap.get(url) == null) {
                PicSimpleBitmapWorkerTask task = new PicSimpleBitmapWorkerTask(this, progressView,
                        wait,
                        error, url, taskMap);
                taskMap.put(url, task);
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                wait.setVisibility(View.VISIBLE);
            } else {
                PicSimpleBitmapWorkerTask task = taskMap.get(url);
                task.bind(this, progressView, wait, error);
            }


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

    private static class PicSimpleBitmapWorkerTask extends MyAsyncTask<String, Integer, String> {

        private FileDownloaderHttpHelper.DownloadListener downloadListener
                = new FileDownloaderHttpHelper.DownloadListener() {
            @Override
            public void pushProgress(int progress, int max) {
                publishProgress(progress, max);
            }


        };

        public void bind(ContainerFragment fragment, CircleProgressView spinner, TextView wait,
                TextView readError) {
            this.containerRefList.add(new WeakReference<ContainerFragment>(fragment));
            this.spinner = spinner;
            this.wait = wait;
            this.readError = readError;
        }

        private List<WeakReference<ContainerFragment>> containerRefList
                = new ArrayList<WeakReference<ContainerFragment>>();

        private TextView wait;

        private String url;

        private CircleProgressView spinner;

        private TextView readError;

        private HashMap<String, PicSimpleBitmapWorkerTask> taskMap;

        public PicSimpleBitmapWorkerTask(ContainerFragment containerFragment,
                CircleProgressView spinner, TextView wait,
                TextView readError, String url,
                HashMap<String, PicSimpleBitmapWorkerTask> taskMap) {
            this.containerRefList.add(new WeakReference<ContainerFragment>(containerFragment));
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
            this.taskMap.remove(url);
            this.spinner.setVisibility(View.INVISIBLE);
            this.wait.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPostExecute(final String bitmapPath) {

            this.taskMap.remove(url);

            if (isCancelled()) {
                return;
            }

            for (WeakReference<ContainerFragment> containerRef : containerRefList) {

                ContainerFragment fragment = containerRef.get();

                if (fragment == null) {
                    continue;
                }

                Activity activity = fragment.getActivity();

                if (activity == null) {
                    continue;
                }

                this.spinner.setVisibility(View.INVISIBLE);
                this.wait.setVisibility(View.INVISIBLE);

                if (TextUtils.isEmpty(bitmapPath)) {
                    readError.setVisibility(View.VISIBLE);
                    readError.setText(
                            activity.getString(R.string.picture_cant_download_or_sd_cant_read));
                } else if (!ImageUtility.isThisBitmapCanRead(bitmapPath)) {
                    readError.setVisibility(View.VISIBLE);
                    readError.setText(
                            activity.getString(
                                    R.string.download_finished_but_cant_read_picture_file));
                } else {
                    readError.setVisibility(View.INVISIBLE);
                    fragment.displayPicture(bitmapPath, false);
                }


            }
        }
    }

    public LongClickListener getLongClickListener() {
        String url = getArguments().getString("url");
        String path = FileManager.getFilePathFromUrl(url, FileLocationMethod.picture_large);
        LongClickListener longClickListener = new LongClickListener(getActivity(), url, path);
        return longClickListener;
    }


}
