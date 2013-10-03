package org.qii.weiciyuan.support.asyncdrawable;

import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: qii
 * Date: 13-2-9
 * support to insert progressbar update
 */
public class DownloadWorker extends MyAsyncTask<String, Integer, Boolean> implements IPictureWorker {


    private String url = "";
    private CopyOnWriteArrayList<FileDownloaderHttpHelper.DownloadListener> downloadListenerList = new CopyOnWriteArrayList<FileDownloaderHttpHelper.DownloadListener>();

    private FileLocationMethod method;

    public String getUrl() {
        return url;
    }

    public DownloadWorker(String url, FileLocationMethod method) {

        this.url = url;
        this.method = method;
    }


    public void addDownloadListener(FileDownloaderHttpHelper.DownloadListener listener) {
        downloadListenerList.addIfAbsent(listener);
    }


    @Override
    protected Boolean doInBackground(String... d) {

        synchronized (TimeLineBitmapDownloader.pauseDownloadWorkLock) {
            while (TimeLineBitmapDownloader.pauseDownloadWork && !isCancelled()) {
                try {
                    TimeLineBitmapDownloader.pauseDownloadWorkLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (isCancelled())
            return false;

        String filePath = FileManager.getFilePathFromUrl(url, method);

        String actualDownloadUrl = url;

        switch (method) {
            case picture_thumbnail:
                actualDownloadUrl = url.replace("thumbnail", "webp180");
                break;
            case picture_bmiddle:
                actualDownloadUrl = url.replace("bmiddle", "webp720");
                break;
            case picture_large:
                actualDownloadUrl = url.replace("large", "woriginal");
                break;

        }

        boolean result = ImageUtility.getBitmapFromNetWork(actualDownloadUrl, filePath, new FileDownloaderHttpHelper.DownloadListener() {
            @Override
            public void pushProgress(int progress, int max) {
                publishProgress(progress, max);
            }
        });
        TaskCache.removeDownloadTask(url, DownloadWorker.this);
        return result;

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        for (FileDownloaderHttpHelper.DownloadListener downloadListener : downloadListenerList) {
            if (downloadListener != null)
                downloadListener.pushProgress(values[0], values[1]);
        }
    }


}