package org.qii.weiciyuan.support.asyncdrawable;

import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.utils.Utility;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: qii
 * Date: 13-2-9
 */
public class TaskCache {

    private static final ThreadFactory sDownloadThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "DownloadFutureTask Download #" + mCount.getAndIncrement());
        }
    };

    private static final Executor DOWNLOAD_THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(4, 4, 1,
            TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(15) {
        @Override
        public boolean offer(Runnable runnable) {
            return super.offerFirst(runnable);
        }
    }, sDownloadThreadFactory,
            new ThreadPoolExecutor.DiscardOldestPolicy() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                    if (!e.isShutdown()) {
                        LinkedBlockingDeque<Runnable> deque = (LinkedBlockingDeque) e.getQueue();
                        Runnable runnable = deque.pollLast();
                        if (runnable instanceof DownloadFutureTask) {
                            DownloadFutureTask futureTask = (DownloadFutureTask) runnable;
                            futureTask.cancel(true);
                            String url = futureTask.getUrl();
                            removeDownloadTask(url, futureTask);
                        }
                        e.execute(r);
                    }
                }
            }
    );

    private static ConcurrentHashMap<String, DownloadFutureTask> downloadTasks
            = new ConcurrentHashMap<String, DownloadFutureTask>();

    public static final Object backgroundWifiDownloadPicturesWorkLock = new Object();

    public static void removeDownloadTask(String url, DownloadFutureTask downloadWorker) {
        synchronized (TaskCache.backgroundWifiDownloadPicturesWorkLock) {
            downloadTasks.remove(url, downloadWorker);
            if (TaskCache.isDownloadTaskFinished()) {
                TaskCache.backgroundWifiDownloadPicturesWorkLock.notifyAll();
            }
        }
    }

    public static boolean isDownloadTaskFinished() {
        return TaskCache.downloadTasks.isEmpty();
    }

    public static boolean isThisUrlTaskFinished(String url) {
        return !downloadTasks.containsKey(url);
    }

    public static boolean waitForPictureDownload(String url,
            FileDownloaderHttpHelper.DownloadListener downloadListener, String savedPath,
            FileLocationMethod method) {
        while (true) {
            DownloadFutureTask downloadFutureTask = TaskCache.downloadTasks.get(url);

            if (downloadFutureTask == null) {

                DownloadFutureTask newDownloadFutureTask = DownloadFutureTask
                        .newInstance(url, method);
                synchronized (backgroundWifiDownloadPicturesWorkLock) {
                    downloadFutureTask = TaskCache.downloadTasks
                            .putIfAbsent(url, newDownloadFutureTask);
                }
                if (downloadFutureTask == null) {
                    downloadFutureTask = newDownloadFutureTask;
                    DOWNLOAD_THREAD_POOL_EXECUTOR.execute(downloadFutureTask);
                }
            }

            downloadFutureTask.addDownloadListener(downloadListener);

            try {
                return downloadFutureTask.get();
            } catch (InterruptedException e) {
                Utility.printStackTrace(e);
                //for better listview scroll performance
                downloadFutureTask.cancel(true);
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                Utility.printStackTrace(e);
                return false;
            } catch (CancellationException e) {
                removeDownloadTask(url, downloadFutureTask);
            }
        }
    }
}
