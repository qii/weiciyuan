package org.qii.weiciyuan.ui.browser;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * User: qii
 * Date: 13-1-4
 */
public class AppMapActivity extends AbstractAppActivity {

    private GoogleMap mMap;
    private double lat;
    private double lon;
    private String locationStr;

    private Marker melbourne;

    private GetGoogleLocationInfo locationTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setTitle(getString(R.string.browser_map));
        lat = getIntent().getDoubleExtra("lat", 0);
        lon = getIntent().getDoubleExtra("lon", 0);
        locationStr = getIntent().getStringExtra("locationStr");
        setUpMapIfNeeded();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(locationStr)) {
            if (Utility.isTaskStopped(locationTask)) {
                GeoBean geoBean = new GeoBean();
                geoBean.setLatitude(lat);
                geoBean.setLongitude(lon);
                locationTask = new GetGoogleLocationInfo(geoBean);
                locationTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utility.cancelTasks(locationTask);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                final LatLng MELBOURNE = new LatLng(lat, lon);
                melbourne = mMap.addMarker(new MarkerOptions()
                        .position(MELBOURNE)
                        .title(!TextUtils.isEmpty(locationStr) ? locationStr : String.format("[%f,%f]", lat, lon))
                        .snippet(String.format("[%f,%f]", lat, lon)
                        ));
                melbourne.showInfoWindow();
                LatLng latLng = new LatLng(lat, lon);
                CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
                mMap.moveCamera(update);

            }
        }
    }

    private class GetGoogleLocationInfo extends MyAsyncTask<Void, String, String> {

        GeoBean geoBean;

        public GetGoogleLocationInfo(GeoBean geoBean) {
            this.geoBean = geoBean;

        }

        @Override
        protected String doInBackground(Void... params) {

            Geocoder geocoder = new Geocoder(AppMapActivity.this, Locale.getDefault());

            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(geoBean.getLat(), geoBean.getLon(), 1);
            } catch (IOException e) {
                cancel(true);
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                StringBuilder builder = new StringBuilder();
                int size = address.getMaxAddressLineIndex();
                for (int i = 0; i < size; i++) {
                    builder.append(address.getAddressLine(i));
                }
                return builder.toString();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            if (!TextUtils.isEmpty(s) && melbourne != null) {
                melbourne.setTitle(s);
                melbourne.showInfoWindow();
            }
            super.onPostExecute(s);
        }
    }
}