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
import java.util.HashSet;

/**
 * User: qii
 * Date: 13-7-28
 */
public class GalleryActivity extends Activity {

    private ArrayList<String> urls = new ArrayList<String>();

    private TextView position;

    private HashMap<String, PicSimpleBitmapWorkerTask> taskMap = new HashMap<String, PicSimpleBitmapWorkerTask>();

    private PicSaveTask saveTask;

    private ViewPager pager;

    private HashSet<ViewGroup> unRecycledViews = new HashSet<ViewGroup>();

    private boolean alreadyShowPicturesTooLargeHint = false;

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

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ImagePagerAdapter());
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                GalleryActivity.this.position.setText(String.valueOf(position + 1));
            }
        });
        pager.setCurrentItem(getIntent().getIntExtra("position", 0));
        pager.setOffscreenPageLimit(3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (String url : urls) {
            MyAsyncTask task = taskMap.get(url);
            if (task != null)
                task.cancel(true);
        }
        Utility.recycleViewGroupAndChildViews(pager, true);
        for (ViewGroup viewGroup : unRecycledViews) {
            Utility.recycleViewGroupAndChildViews(viewGroup, true);
        }

        System.gc();
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
            if (object instanceof ViewGroup) {
                ((ViewPager) container).removeView((View) object);
                unRecycledViews.remove(object);
                ViewGroup viewGroup = (ViewGroup) object;
                Utility.recycleViewGroupAndChildViews(viewGroup, true);

            }
//            ((ViewPager) container).removeView((View) object);
        }


        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View contentView = inflater.inflate(R.layout.galleryactivity_item, view, false);

            handlePage(position, contentView);

            ((ViewPager) view).addView(contentView, 0);
            unRecycledViews.add((ViewGroup) contentView);
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

            handlePage(position, contentView);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }


    }

    private void handlePage(int position, View contentView) {

        PhotoView imageView = (PhotoView) contentView.findViewById(R.id.image);
        imageView.setVisibility(View.INVISIBLE);

        imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                GalleryActivity.this.finish();
            }
        });


        TextView readError = (TextView) contentView.findViewById(R.id.error);

        String path = FileManager.getFilePathFromUrl(urls.get(position), FileLocationMethod.picture_large);

        if (ImageTool.isThisBitmapCanRead(path)) {

            readPicture(imageView, readError, urls.get(position), path);

        } else if (Utility.isWifi(GalleryActivity.this)) {

            final CircleProgressView spinner = (CircleProgressView) contentView.findViewById(R.id.loading);
            spinner.setVisibility(View.VISIBLE);

            if (taskMap.get(urls.get(position)) == null) {
                PicSimpleBitmapWorkerTask task = new PicSimpleBitmapWorkerTask(imageView, spinner, readError, urls.get(position), taskMap);
                taskMap.put(urls.get(position), task);
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                PicSimpleBitmapWorkerTask task = taskMap.get(urls.get(position));
                task.setWidget(imageView, spinner);
            }
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
        private TextView readError;
        private HashMap<String, PicSimpleBitmapWorkerTask> taskMap;

        public PicSimpleBitmapWorkerTask(ImageView iv, CircleProgressView spinner,
                                         TextView readError, String url, HashMap<String, PicSimpleBitmapWorkerTask> taskMap) {
            this.iv = iv;
            this.url = url;
            this.spinner = spinner;
            this.readError = readError;
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

            if (TextUtils.isEmpty(bitmapPath) || iv == null)
                return;

            if (!ImageTool.isThisBitmapCanRead(bitmapPath)) {
                Toast.makeText(GalleryActivity.this, R.string.download_finished_but_cant_read_picture_file, Toast.LENGTH_SHORT).show();
            }

            readPicture(iv, readError, url, bitmapPath);


        }
    }


    private void readPicture(ImageView imageView, TextView readError, String url, String bitmapPath) {

        if (!ImageTool.isThisBitmapCanRead(bitmapPath)) {
            Toast.makeText(GalleryActivity.this, R.string.download_finished_but_cant_read_picture_file, Toast.LENGTH_SHORT).show();
        }


        boolean isThisBitmapTooLarge = ImageTool.isThisBitmapTooLargeToRead(bitmapPath);
        if (isThisBitmapTooLarge && !alreadyShowPicturesTooLargeHint) {
            Toast.makeText(GalleryActivity.this, R.string.picture_is_too_large_so_enable_software_layer, Toast.LENGTH_LONG).show();
            alreadyShowPicturesTooLargeHint = true;
        }

        if (isThisBitmapTooLarge) {
            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            PhotoView photoView = (PhotoView) imageView;
            photoView.setMaxScale(15);
        }

        Bitmap bitmap = null;
        try {
            bitmap = ImageTool.decodeBitmapFromSDCard(bitmapPath, 2000, 3000);
        } catch (OutOfMemoryError ignored) {

        }

        if (bitmap != null) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);
            bindImageViewLongClickListener(imageView, url, bitmapPath);
            readError.setVisibility(View.INVISIBLE);
        } else {
            imageView.setVisibility(View.INVISIBLE);
            readError.setVisibility(View.VISIBLE);
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