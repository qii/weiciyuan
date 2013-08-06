package org.qii.weiciyuan.support.gallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.asyncdrawable.TaskCache;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.CircleProgressView;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.Utility;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.File;
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

    private PicSaveTask saveTask;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galleryactivity_layout);

        position = (TextView) findViewById(R.id.position);
        TextView sum = (TextView) findViewById(R.id.sum);

        MessageBean msg = getIntent().getParcelableExtra("msg");
        ArrayList<String> tmp = msg.getThumbnailPicUrls();
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
        pager.setCurrentItem(getIntent().getIntExtra("position", 0));
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
            View contentView = inflater.inflate(R.layout.galleryactivity_item, view, false);

            PhotoView imageView = (PhotoView) contentView.findViewById(R.id.image);
            imageView.setVisibility(View.INVISIBLE);

            imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    GalleryActivity.this.finish();
                }
            });


            String path = FileManager.getFilePathFromUrl(urls.get(position), FileLocationMethod.picture_large);

            if (ImageTool.isThisBitmapCanRead(path)) {
                Bitmap bitmap = ImageTool.decodeBitmapFromSDCard(path, -1, -1);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                bindImageViewLongClickListener(imageView, urls.get(position), path);
            } else if (Utility.isWifi(GalleryActivity.this)) {

                final CircleProgressView spinner = (CircleProgressView) contentView.findViewById(R.id.loading);
                spinner.setVisibility(View.VISIBLE);

                if (taskMap.get(urls.get(position)) == null) {
                    PicSimpleBitmapWorkerTask task = new PicSimpleBitmapWorkerTask(imageView, spinner, urls.get(position), taskMap);
                    taskMap.put(urls.get(position), task);
                    task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    PicSimpleBitmapWorkerTask task = taskMap.get(urls.get(position));
                    task.setWidget(imageView, spinner);
                }
            }

            ((ViewPager) view).addView(contentView, 0);
            return contentView;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            View contentView = (View) object;
            if (contentView == null)
                return;
            ImageView imageView = (ImageView) contentView.findViewById(R.id.image);

            if (imageView.getDrawable() != null)
                return;

            imageView.setVisibility(View.INVISIBLE);

            final CircleProgressView spinner = (CircleProgressView) contentView.findViewById(R.id.loading);
            spinner.setVisibility(View.VISIBLE);

            if (taskMap.get(urls.get(position)) == null) {
                PicSimpleBitmapWorkerTask task = new PicSimpleBitmapWorkerTask(imageView, spinner, urls.get(position), taskMap);
                taskMap.put(urls.get(position), task);
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                PicSimpleBitmapWorkerTask task = taskMap.get(urls.get(position));
                task.setWidget(imageView, spinner);
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }


    }


    private class PicSimpleBitmapWorkerTask extends MyAsyncTask<String, Integer, String> {

        private FileDownloaderHttpHelper.DownloadListener downloadListener = new FileDownloaderHttpHelper.DownloadListener() {
            @Override
            public void pushProgress(int progress, int max) {
                publishProgress(progress, max);
            }


        };

        public void setWidget(ImageView iv, CircleProgressView spinner) {
            this.iv = iv;
            this.spinner = spinner;
        }

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
                iv.setVisibility(View.VISIBLE);
                Bitmap bitmap = ImageTool.decodeBitmapFromSDCard(bitmapPath, -1, -1);
                iv.setImageBitmap(bitmap);
                bindImageViewLongClickListener(iv, url, bitmapPath);

            }

        }
    }


    private void bindImageViewLongClickListener(ImageView imageView, final String url, final String filePath) {

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                String[] values = {getString(R.string.copy_link_to_clipboard), getString(R.string.share), getString(R.string.save_pic_album)};

                new AlertDialog.Builder(GalleryActivity.this)
                        .setItems(values, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", url));
                                        Toast.makeText(GalleryActivity.this, getString(R.string.copy_successfully), Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                        sharingIntent.setType("image/jpeg");
                                        if (!TextUtils.isEmpty(filePath)) {
                                            Uri uri = Uri.fromFile(new File(filePath));
                                            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                            if (Utility.isIntentSafe(GalleryActivity.this, sharingIntent)) {
                                                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)));
                                            }
                                        }
                                        break;
                                    case 2:
                                        saveBitmapToPictureDir(filePath);
                                        break;
                                }
                            }
                        }).show();

                return true;
            }
        });
    }


    private void saveBitmapToPictureDir(String filePath) {
        if (Utility.isTaskStopped(saveTask)) {
            saveTask = new PicSaveTask(filePath);
            saveTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }

    }


    private class PicSaveTask extends MyAsyncTask<Void, Boolean, Boolean> {

        String path;

        public PicSaveTask(String path) {
            this.path = path;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return FileManager.saveToPicDir(path);
        }


        @Override
        protected void onPostExecute(Boolean value) {
            super.onPostExecute(value);
            if (value)
                Toast.makeText(GalleryActivity.this, getString(R.string.save_to_album_successfully), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(GalleryActivity.this, getString(R.string.cant_save_pic), Toast.LENGTH_SHORT).show();
        }


    }
}