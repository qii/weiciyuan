package org.qii.weiciyuan.support.asyncdrawable;

import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import java.io.File;
import java.util.concurrent.*;

/**
 * User: qii
 * Date: 13-2-9
 */
public class TaskCache {

    private static ConcurrentHashMap<String, DownloadWorker> downloadTasks = new ConcurrentHashMap<String, DownloadWorker>();

    public static void removeDownloadTask(String url, DownloadWorker downloadWorker) {

        downloadTasks.remove(url, downloadWorker);

    }

    public static boolean waitForPictureDownload(String url, FileDownloaderHttpHelper.DownloadListener downloadListener, String savedPath, FileLocationMethod method) {
        while (true) {
            DownloadWorker downloadWorker = TaskCache.downloadTasks.get(url);
            boolean localFileExist = new File(savedPath).exists();

            if (downloadWorker == null) {
                if (!localFileExist) {
                    DownloadWorker newWorker = new DownloadWorker(url, method);
                    downloadWorker = TaskCache.downloadTasks.putIfAbsent(url, newWorker);
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
                e.printStackTrace();
                return false;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            } catch (TimeoutException e) {
                e.printStackTrace();
                return false;
            } catch (CancellationException e) {
                downloadTasks.remove(url, downloadWorker);
            }

        }
    }

    /**
     * todo
     *
     * @param msg
     * @param downloadListener
     */
    public static void waitForMsgDetailPictureDownload(MessageBean msg, FileDownloaderHttpHelper.DownloadListener downloadListener) {
        while (true) {
            DownloadWorker downloadWorker = null;


            FileLocationMethod method;
            String middleUrl = msg.getBmiddle_pic();
            String largeUrl = msg.getOriginal_pic();
            String middlePath = FileManager.getFilePathFromUrl(msg.getBmiddle_pic(), FileLocationMethod.picture_bmiddle);
            String largePath = FileManager.getFilePathFromUrl(msg.getOriginal_pic(), FileLocationMethod.picture_large);


            downloadWorker = TaskCache.downloadTasks.get(largeUrl);
            boolean localFileExist = new File(largePath).exists();

            if (downloadWorker == null) {
                TaskCache.downloadTasks.get(middleUrl);
            }

            if (downloadWorker == null) {
                if (localFileExist) {
                    return;
                } else {
                    localFileExist = new File(middlePath).exists();
                    if (localFileExist) {
                        return;
                    }
                }


                DownloadWorker newWorker = new DownloadWorker(middleUrl, FileLocationMethod.picture_bmiddle);
                downloadWorker = TaskCache.downloadTasks.putIfAbsent(middleUrl, newWorker);
                if (downloadWorker == null) {
                    downloadWorker = newWorker;
                    downloadWorker.executeOnExecutor(MyAsyncTask.DOWNLOAD_THREAD_POOL_EXECUTOR);
                }
            }


            try {
                downloadWorker.addDownloadListener(downloadListener);
                downloadWorker.get(30, TimeUnit.SECONDS);
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return;
            } catch (TimeoutException e) {
                e.printStackTrace();
                return;
            } catch (CancellationException e) {
                downloadTasks.remove(middleUrl, downloadWorker);
            }

        }
    }

}
