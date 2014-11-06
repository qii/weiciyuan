package org.qii.weiciyuan.support.asyncdrawable;

import org.qii.weiciyuan.support.database.DownloadPicturesDBTask;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;

import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;

/**
 * User: qii
 * Date: 14-6-10
 */
public class DownloadFutureTask extends FutureTask<Boolean> {

    public static DownloadFutureTask newInstance(String url, FileLocationMethod method) {
        JobCallable jobCallable = new JobCallable(url, method);
        DownloadFutureTask downloadFutureTask = new DownloadFutureTask(jobCallable);
        jobCallable.futureTask = downloadFutureTask;
        return downloadFutureTask;
    }

    private JobCallable callable;

    private DownloadFutureTask(JobCallable callable) {
        super(callable);
        this.callable = callable;
    }

    public void addDownloadListener(FileDownloaderHttpHelper.DownloadListener listener) {
        callable.addDownloadListener(listener);
    }

    public String getUrl() {
        return callable.url;
    }

    private static class JobCallable implements Callable<Boolean> {

        private DownloadFutureTask futureTask;

        private CopyOnWriteArrayList<FileDownloaderHttpHelper.DownloadListener> downloadListenerList
                = new CopyOnWriteArrayList<FileDownloaderHttpHelper.DownloadListener>();

        private String url;

        private FileLocationMethod method;

        private int progress;
        private int max;

        public void addDownloadListener(FileDownloaderHttpHelper.DownloadListener listener) {
            if (listener == null) {
                return;
            }
            downloadListenerList.addIfAbsent(listener);
            if (progress > 0 && max > 0) {
                listener.pushProgress(progress, max);
            }
        }

        private JobCallable(String url, FileLocationMethod method) {
            this.url = url;
            this.method = method;
        }

        @Override
        public Boolean call() throws Exception {
            synchronized (TimeLineBitmapDownloader.pauseDownloadWorkLock) {
                while (TimeLineBitmapDownloader.pauseDownloadWork && !Thread.currentThread()
                        .isInterrupted()) {
                    try {
                        TimeLineBitmapDownloader.pauseDownloadWorkLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            String filePath = FileManager.generateDownloadFileName(url);

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

            boolean result = ImageUtility.getBitmapFromNetWork(actualDownloadUrl, filePath,
                    new FileDownloaderHttpHelper.DownloadListener() {
                        @Override
                        public void pushProgress(int progress, int max) {
                            JobCallable.this.progress = progress;
                            JobCallable.this.max = max;
                            for (FileDownloaderHttpHelper.DownloadListener downloadListener : downloadListenerList) {
                                if (downloadListener != null) {
                                    downloadListener.pushProgress(progress, max);
                                }
                            }
                        }
                    });

            if (result) {
                DownloadPicturesDBTask.add(this.url,
                        FileManager.generateDownloadFileName(this.url),
                        this.method);
            }

            TaskCache.removeDownloadTask(url, futureTask);
            return result;
        }
    }
}
