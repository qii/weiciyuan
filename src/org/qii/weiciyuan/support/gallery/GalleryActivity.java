package org.qii.weiciyuan.support.gallery;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.asyncdrawable.TaskCache;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.CircleProgressView;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: qii
 * Date: 13-7-28
 */
public class GalleryActivity extends Activity {

    private ArrayList<String> urls = new ArrayList<String>();

    private TextView position;

    private HashMap<String, PicSimpleBitmapWorkerTask> taskMap = new HashMap<String, PicSimpleBitmapWorkerTask>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galleryactivity_layout);

        position = (TextView) findViewById(R.id.position);
        TextView sum = (TextView) findViewById(R.id.sum);

        MessageBean msg = getIntent().getParcelableExtra("msg");
        ArrayList<String> tmp = msg.getPicUrls();
        for (int i = 0; i < tmp.size(); i++) {
            urls.add(tmp.get(i).replace("thumbnail", "large"));
        }
        sum.setText(String.valueOf(urls.size()));

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ImagePagerAdapter());
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                GalleryActivity.this.position.setText(String.valueOf(position + 1));
            }
        });
        pager.setCurrentItem(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (String url : urls) {
            MyAsyncTask task = taskMap.get(url);
            if (task != null)
                task.cancel(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    private class ImagePagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        public ImagePagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            ((ViewPager) container).removeView((View) object);
        }


        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.galleryactivity_item, view, false);

            String path = FileManager.getFilePathFromUrl(urls.get(position), FileLocationMethod.picture_large);

            if (ImageTool.isThisBitmapCanRead(path)) {
                ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
                Bitmap bitmap = ImageTool.decodeBitmapFromSDCard(path, -1, -1);
                imageView.setImageBitmap(bitmap);
            }

            ((ViewPager) view).addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            View imageLayout = (View) object;
            if (imageLayout == null)
                return;
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);

            if (imageView.getDrawable() != null)
                return;

            final CircleProgressView spinner = (CircleProgressView) imageLayout.findViewById(R.id.loading);
            spinner.setVisibility(View.VISIBLE);

            if (taskMap.get(urls.get(position)) == null) {
                PicSimpleBitmapWorkerTask task = new PicSimpleBitmapWorkerTask(imageView, spinner, urls.get(position), taskMap);
                taskMap.put(urls.get(position), task);
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }


    }


    private static class PicSimpleBitmapWorkerTask extends MyAsyncTask<String, Integer, String> {

        private FileDownloaderHttpHelper.DownloadListener downloadListener = new FileDownloaderHttpHelper.DownloadListener() {
            @Override
            public void pushProgress(int progress, int max) {
                publishProgress(progress, max);
            }


        };
        private ImageView iv;
        private String url;
        private CircleProgressView spinner;
        private HashMap<String, PicSimpleBitmapWorkerTask> taskMap;

        public PicSimpleBitmapWorkerTask(ImageView iv, CircleProgressView spinner, String url, HashMap<String, PicSimpleBitmapWorkerTask> taskMap) {
            this.iv = iv;
            this.url = url;
            this.spinner = spinner;
            this.taskMap = taskMap;
        }


        @Override
        protected String doInBackground(String... dd) {
            if (isCancelled()) {
                return null;
            }

            TaskCache.waitForMsgDetailPictureDownload(url, downloadListener);

            String path = FileManager.getFilePathFromUrl(url, FileLocationMethod.picture_large);
            return path;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            int max = values[1];
            spinner.setMax(max);
            spinner.setProgress(progress);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            taskMap.remove(url);
        }

        @Override
        protected void onPostExecute(final String bitmapPath) {
            if (isCancelled()) {
                return;
            }

            taskMap.remove(url);

            if (!TextUtils.isEmpty(bitmapPath) && iv != null) {
                Bitmap bitmap = ImageTool.decodeBitmapFromSDCard(bitmapPath, -1, -1);
                iv.setImageBitmap(bitmap);

            }

        }
    }


    private static class PicSimpleBitmapReaderWorkerTask extends MyAsyncTask<String, Integer, String> {

        private ImageView iv;
        private String url;
        private CircleProgressView spinner;
        private HashMap<String, PicSimpleBitmapWorkerTask> taskMap;

        public PicSimpleBitmapReaderWorkerTask(ImageView iv, CircleProgressView spinner, String url, HashMap<String, PicSimpleBitmapWorkerTask> taskMap) {
            this.iv = iv;
            this.url = url;
            this.spinner = spinner;
            this.taskMap = taskMap;
        }


        @Override
        protected String doInBackground(String... dd) {
            if (isCancelled()) {
                return null;
            }


            String path = FileManager.getFilePathFromUrl(url, FileLocationMethod.picture_large);
            return path;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            int max = values[1];
            spinner.setMax(max);
            spinner.setProgress(progress);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            taskMap.remove(url);
        }

        @Override
        protected void onPostExecute(final String bitmapPath) {
            if (isCancelled()) {
                return;
            }

            taskMap.remove(url);

            if (!TextUtils.isEmpty(bitmapPath) && iv != null) {
                Bitmap bitmap = ImageTool.decodeBitmapFromSDCard(bitmapPath, -1, -1);
                iv.setImageBitmap(bitmap);

            }

        }
    }

}