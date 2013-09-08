package org.qii.weiciyuan.support.asyncdrawable;

import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.Utility;

import java.io.File;
import java.util.concurrent.*;

/**
 * User: qii
 * Date: 13-2-9
 * ReadWorker can be interrupted, DownloadWorker can't be interrupted, maybe I can remove CancellationException exception?
 */
public class TaskCache {

    private static ConcurrentHashMap<String, DownloadWorker> downloadTasks = new ConcurrentHashMap<String, DownloadWorker>();

    public static final Object backgroundWifiDownloadPicturesWorkLock = new Object();


    public static void removeDownloadTask(String url, DownloadWorker downloadWorker) {
        synchronized (TaskCache.backgroundWifiDownloadPicturesWorkLock) {
            downloadTasks.remove(url, downloadWorker);
            if (TaskCache.isDownloadTaskFinished())
                TaskCache.backgroundWifiDownloadPicturesWorkLock.notifyAll();
        }

    }


    public static boolean isDownloadTaskFinished() {
        return TaskCache.downloadTasks.isEmpty();
    }

    public static boolean isThisUrlTaskFinished(String url) {
        return !downloadTasks.containsKey(url);
    }


    public static boolean waitForPictureDownload(String url, FileDownloaderHttpHelper.DownloadListener downloadListener, String savedPath, FileLocationMethod method) {
        while (true) {
            DownloadWorker downloadWorker = TaskCache.downloadTasks.get(url);
            boolean localFileExist = new File(savedPath).exists();

            if (downloadWorker == null) {
                if (!localFileExist) {
                    DownloadWorker newWorker = new DownloadWorker(url, method);
                    synchronized (backgroundWifiDownloadPicturesWorkLock) {
                        downloadWorker = TaskCache.downloadTasks.putIfAbsent(url, newWorker);
                    }
                    if (downloadWorker == null) {
                        downloadWorker = newWorker;
                        downloadWorker.executeOnExecutor(MyAsyncTask.DOWNLOAD_THREAD_POOL_EXECUTOR);
                    }
                } else {
                    return true;
                }
            }

            downloadWorker.addDownloadListener(downloadListener);

            try {
                return downloadWorker.get(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Utility.printStackTrace(e);
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                Utility.printStackTrace(e);
                return false;
            } catch (TimeoutException e) {
                Utility.printStackTrace(e);
                return false;
            } catch (CancellationException e) {
                removeDownloadTask(url, downloadWorker);
            }

        }
    }


    public static boolean waitForMsgDetailPictureDownload(String url, FileDownloaderHttpHelper.DownloadListener downloadListener) {
        while (true) {
            DownloadWorker downloadWorker = null;


            String largePath = FileManager.getFilePathFromUrl(url, FileLocationMethod.picture_large);


            downloadWorker = TaskCache.downloadTasks.get(url);
            boolean localFileExist = new File(largePath).exists();


            if (downloadWorker == null) {
                if (localFileExist) {
                    return true;
                }

                DownloadWorker newWorker = new DownloadWorker(url, FileLocationMethod.picture_large);
                synchronized (backgroundWifiDownloadPicturesWorkLock) {
                    downloadWorker = TaskCache.downloadTasks.putIfAbsent(url, newWorker);

                }
                if (downloadWorker == null) {
                    downloadWorker = newWorker;
                    downloadWorker.executeOnExecutor(MyAsyncTask.DOWNLOAD_THREAD_POOL_EXECUTOR);
                }
            }


            try {
                downloadWorker.addDownloadListener(downloadListener);
                return downloadWorker.get(30, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Utility.printStackTrace(e);
                return false;
            } catch (ExecutionException e) {
                Utility.printStackTrace(e);
                return false;
            } catch (TimeoutException e) {
                Utility.printStackTrace(e);
                return false;
            } catch (CancellationException e) {
                removeDownloadTask(url, downloadWorker);
            }

        }
    }

}
