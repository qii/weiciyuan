package org.qii.weiciyuan.ui.browser;

import android.app.Activity;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.dao.map.MapDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * User: qii
 * Date: 13-1-25
 */
public class GetWeiboLocationInfoTask extends MyAsyncTask<Void, String, Bitmap> {

    private Activity activity;
    private TextView location;
    private ImageView mapView;

    private GeoBean geoBean;

    public GetWeiboLocationInfoTask(Activity activity, GeoBean geoBean, ImageView mapView, TextView location) {
        this.geoBean = geoBean;
        this.activity = activity;
        this.mapView = mapView;
        this.location = location;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        location.setVisibility(View.VISIBLE);
        location.setText(String.valueOf(geoBean.getLat() + "," + geoBean.getLon()));

    }

    @Override
    protected Bitmap doInBackground(Void... params) {

        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());

        List<Address> addresses = null;
        try {
            if (!Utility.isGPSLocationCorrect(geoBean)) {
                publishProgress("");
            }
            addresses = geocoder.getFromLocation(geoBean.getLat(), geoBean.getLon(), 1);
        } catch (IOException e) {
//            cancel(true);
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);

            StringBuilder builder = new StringBuilder();
            int size = address.getMaxAddressLineIndex();
            for (int i = 0; i < size; i++) {
                builder.append(address.getAddressLine(i));
            }
            publishProgress(builder.toString());
        }

        MapDao dao = new MapDao(GlobalContext.getInstance().getSpecialToken(), geoBean.getLat(), geoBean.getLon());

        try {
            return dao.getMap();
        } catch (WeiboException e) {
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (!TextUtils.isEmpty(values[0])) {
            location.setVisibility(View.VISIBLE);
            location.setText(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Bitmap s) {
        mapView.setImageBitmap(s);
        super.onPostExecute(s);
    }
}
