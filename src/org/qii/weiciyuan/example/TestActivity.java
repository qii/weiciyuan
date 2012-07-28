/*
 * Copyright 2011 Sina.
 *
 * Licensed under the Apache License and Weibo License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.open.weibo.com
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qii.weiciyuan.example;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.weibo.Utility;
import org.qii.weiciyuan.weibo.Weibo;
import org.qii.weiciyuan.weibo.WeiboException;
import org.qii.weiciyuan.weibo.WeiboParameters;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Sample code for testing weibo APIs.
 *
 * @author ZhangJie (zhangjie2@staff.sina.com.cn)
 */

public class TestActivity extends Activity {
    TextView mResult;
    Weibo mWeibo = Weibo.getInstance();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.timeline);

        mResult = (TextView) this.findViewById(R.id.tvResult);


        new AsyncTask<Void, String, String>() {


            @Override
            protected String doInBackground(Void... params) {
                try {
                    return getPublicTimeline(mWeibo);
                } catch (IOException e) {


                } catch (WeiboException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            protected void onPostExecute(String o) {
                Log.e("dddd", "1" + o);
                mResult.setText(o);
                super.onPostExecute(o);
            }
        }.execute();


    }



    private String getPublicTimeline(Weibo weibo) throws MalformedURLException, IOException,
            WeiboException {
        String url = Weibo.SERVER + "statuses/public_timeline.json";
        WeiboParameters bundle = new WeiboParameters();
        bundle.add("source", Weibo.getAppKey());
        String rlt = weibo.request(this, url, bundle, "GET", mWeibo.getAccessToken());
        return rlt;
    }

    private String upload(Weibo weibo, String source, String file, String status, String lon,
                          String lat) throws WeiboException {
        WeiboParameters bundle = new WeiboParameters();
        bundle.add("source", source);
        bundle.add("pic", file);
        bundle.add("status", status);
        if (!TextUtils.isEmpty(lon)) {
            bundle.add("lon", lon);
        }
        if (!TextUtils.isEmpty(lat)) {
            bundle.add("lat", lat);
        }
        String rlt = "";
        String url = Weibo.SERVER + "statuses/upload.json";
        try {
            rlt = weibo
                    .request(this, url, bundle, Utility.HTTPMETHOD_POST, mWeibo.getAccessToken());
        } catch (WeiboException e) {
            throw new WeiboException(e);
        }
        return rlt;
    }

    private String update(Weibo weibo, String source, String status, String lon, String lat, Object o)
            throws WeiboException {
        WeiboParameters bundle = new WeiboParameters();
        bundle.add("source", source);
        bundle.add("status", status);
        if (!TextUtils.isEmpty(lon)) {
            bundle.add("lon", lon);
        }
        if (!TextUtils.isEmpty(lat)) {
            bundle.add("lat", lat);
        }
        String rlt = "";
        String url = Weibo.SERVER + "statuses/update.json";
        rlt = weibo.request(this, url, bundle, Utility.HTTPMETHOD_POST, mWeibo.getAccessToken());
        return rlt;
    }
}
