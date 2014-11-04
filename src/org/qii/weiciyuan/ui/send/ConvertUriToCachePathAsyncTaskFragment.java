package org.qii.weiciyuan.ui.send;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.Utility;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: qii
 * Date: 13-12-14
 */
public class ConvertUriToCachePathAsyncTaskFragment extends Fragment {

    private ConvertTask task;

    public static ConvertUriToCachePathAsyncTaskFragment newInstance(Uri uri) {
        ConvertUriToCachePathAsyncTaskFragment
                fragment = new ConvertUriToCachePathAsyncTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("uri", uri);
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (task == null) {
            task = new ConvertTask((Uri) getArguments().getParcelable("uri"));
            task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class ConvertTask extends MyAsyncTask<Void, Void, String> {

        ContentResolver mContentResolver;
        Uri uri;

        public ConvertTask(Uri uri) {
            this.uri = uri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mContentResolver = getActivity().getContentResolver();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                InputStream inputStream = mContentResolver.openInputStream(uri);
                String path = FileManager.getKKConvertPicTempFile();
                if (TextUtils.isEmpty(path)) {
                    return null;
                }
                File file = new File(path);
                file.getParentFile().mkdirs();
                if (file.exists() || file.length() > 0) {
                    file.delete();
                }
                file.createNewFile();
                Utility.copyFile(inputStream, file);
                if (ImageUtility.isThisBitmapCanRead(path)) {
                    return path;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (getActivity() == null) {
                return;
            }
            getFragmentManager().beginTransaction()
                    .remove(ConvertUriToCachePathAsyncTaskFragment.this)
                    .commitAllowingStateLoss();
            if (TextUtils.isEmpty(s)) {
                Toast.makeText(getActivity(),
                        getString(R.string.fetch_picture_from_other_apps_failed),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            WriteWeiboActivity activity = (WriteWeiboActivity) getActivity();
            activity.picConvertSucceedKK(s);
        }
    }
}