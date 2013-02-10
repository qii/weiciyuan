package org.qii.weiciyuan.support.file;

public class FileDownloaderHttpHelper {
    public static abstract class DownloadListener {
        public void pushProgress(int progress, int max) {
        }

        ;

        public void completed() {
        }

        ;

        public void cancel() {
        }

        ;
    }
}
