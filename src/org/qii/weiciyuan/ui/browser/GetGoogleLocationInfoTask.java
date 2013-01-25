package org.qii.weiciyuan.ui.browser;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.Utility;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * User: qii
 * Date: 13-1-25
 */
public class GetGoogleLocationInfoTask extends MyAsyncTask<Void, String, String> {

    private Activity activity;
    private TextView location;
    private MapView mapView;

    private GeoBean geoBean;

    public GetGoogleLocationInfoTask(Activity activity, GeoBean geoBean, MapView mapView, TextView location) {
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
        if (Utility.isGooglePlaySafe(activity)) {
            mapView.setVisibility(View.VISIBLE);
            GoogleMap mMap = mapView.getMap();
            if (mMap != null) {

                final LatLng MELBOURNE = new LatLng(geoBean.getLat(), geoBean.getLon());
                Marker melbourne = mMap.addMarker(new MarkerOptions()
                        .position(MELBOURNE));

                LatLng latLng = new LatLng(geoBean.getLat(), geoBean.getLon());
                CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
                mMap.moveCamera(update);

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {

                        Intent intent = new Intent(activity, AppMapActivity.class);
                        intent.putExtra("lat", geoBean.getLat());
                        intent.putExtra("lon", geoBean.getLon());
                        if (!String.valueOf(geoBean.getLat() + "," + geoBean.getLon()).equals(location.getText()))
                            intent.putExtra("locationStr", location.getText());
                        activity.startActivity(intent);
                    }
                });
            }
        } else {
            mapView.setVisibility(View.GONE);
        }
    }

    @Override
    protected String doInBackground(Void... params) {

        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());

        List<Address> addresses = null;
        try {
            if (!Utility.isGPSLocationCorrect(geoBean)) {
                return "";
            }
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
        if (!TextUtils.isEmpty(s)) {
            location.setVisibility(View.VISIBLE);
            location.setText(s);
        }

        super.onPostExecute(s);
    }
}
