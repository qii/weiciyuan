package org.qii.weiciyuan.ui.nearby;

import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.NearbyStatusListBean;
import org.qii.weiciyuan.dao.location.NearbyTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

import java.io.IOException;
import java.util.*;

/**
 * User: qii
 * Date: 13-3-8
 */
public class NearbyTimeLineActivity extends AbstractAppActivity {

    private GoogleMap mMap;
    private double lat;
    private double lon;
    private String locationStr;

    private Marker melbourne;
    private Map<Marker, MessageBean> bindEvent = new HashMap<Marker, MessageBean>();


    private GetGoogleLocationInfo locationTask;
    private FetchWeiboMsg fetchWeiboMsg;

    private MenuItem refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setTitle(getString(R.string.nearby));
        addLocation();
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

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    public void onInfoWindowClick(Marker marker) {
                        MessageBean msg = bindEvent.get(marker);
                        if (msg != null) {
                            Intent intent = new Intent(NearbyTimeLineActivity.this, BrowserWeiboMsgActivity.class);
                            intent.putExtra("msg", msg);
                            intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                            startActivityForResult(intent, 0);
                        }
                    }
                });

                final LatLng MELBOURNE = new LatLng(lat, lon);
                melbourne = mMap.addMarker(new MarkerOptions()
                        .position(MELBOURNE)
                        .title(GlobalContext.getInstance().getCurrentAccountName())
                        .snippet(String.format("[%f,%f]", lat, lon)
                        ));
                melbourne.showInfoWindow();
                LatLng latLng = new LatLng(lat, lon);
                CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
                mMap.moveCamera(update);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_nearbytimelineactivity, menu);
        refresh = menu.findItem(R.id.refresh);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                Set<Marker> markers = bindEvent.keySet();
                for (Marker marker : markers) {
                    marker.remove();
                }
                if (Utility.isTaskStopped(fetchWeiboMsg)) {
                    fetchWeiboMsg = new FetchWeiboMsg();
                    fetchWeiboMsg.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private class GetGoogleLocationInfo extends MyAsyncTask<Void, String, String> {

        GeoBean geoBean;

        public GetGoogleLocationInfo(GeoBean geoBean) {
            this.geoBean = geoBean;

        }

        @Override
        protected String doInBackground(Void... params) {

            Geocoder geocoder = new Geocoder(NearbyTimeLineActivity.this, Locale.getDefault());

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
                melbourne.showInfoWindow();
                getActionBar().setSubtitle(s);
            }
            super.onPostExecute(s);
        }
    }


    private void addLocation() {
        LocationManager locationManager = (LocationManager) NearbyTimeLineActivity.this
                .getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(NearbyTimeLineActivity.this, getString(R.string.please_open_gps), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(NearbyTimeLineActivity.this, getString(R.string.gps_is_searching), Toast.LENGTH_SHORT).show();

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,
                    locationListener);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
                    locationListener);
    }


    private void updateWithNewLocation(Location result) {
        GeoBean geoBean = new GeoBean();
        lat = result.getLatitude();
        lon = result.getLongitude();
        setUpMapIfNeeded();
        geoBean.setLatitude(lat);
        geoBean.setLongitude(lon);
        if (Utility.isTaskStopped(locationTask)) {
            locationTask = new GetGoogleLocationInfo(geoBean);
            locationTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (Utility.isTaskStopped(fetchWeiboMsg)) {
            fetchWeiboMsg = new FetchWeiboMsg();
            fetchWeiboMsg.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }

        ((LocationManager) NearbyTimeLineActivity.this
                .getSystemService(Context.LOCATION_SERVICE)).removeUpdates(locationListener);

    }


    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);

        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
        }
    };

    private class FetchWeiboMsg extends MyAsyncTask<Void, Void, NearbyStatusListBean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ProgressBar pb = (ProgressBar) inflater.inflate(R.layout.editmyprofileactivity_refresh_actionbar_view_layout, null);
            refresh.setActionView(pb);
        }

        @Override
        protected NearbyStatusListBean doInBackground(Void... params) {

            try {
                return new NearbyTimeLineDao(GlobalContext.getInstance().getSpecialToken(), lat, lon).get();
            } catch (WeiboException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(NearbyStatusListBean nearbyStatusListBean) {
            super.onPostExecute(nearbyStatusListBean);
            if (refresh.getActionView() != null) {
                refresh.getActionView().clearAnimation();
                refresh.setActionView(null);
            }

            if (nearbyStatusListBean == null)
                return;
            List<MessageBean> messageBeanList = nearbyStatusListBean.getItemList();
            for (MessageBean msg : messageBeanList) {
                GeoBean g = msg.getGeo();
                if (g == null) {
                    continue;
                }
                final LatLng MELBOURNE = new LatLng(g.getLat(), g.getLon());
                Marker melbourne = mMap.addMarker(new MarkerOptions()
                        .position(MELBOURNE)
                        .title(msg.getUser().getScreen_name())
                        .snippet(msg.getText())
                );
                melbourne.showInfoWindow();
                bindEvent.put(melbourne, msg);
            }
        }
    }
}