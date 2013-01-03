package org.qii.weiciyuan.ui.browser;

import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 13-1-4
 */
public class AppMapActivity extends AbstractAppActivity {

    private GoogleMap mMap;
    private double lat;
    private double lon;
    private String locationStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        getActionBar().setTitle(getString(R.string.browser_map));
        lat = getIntent().getDoubleExtra("lat", 0);
        lon = getIntent().getDoubleExtra("lon", 0);
        locationStr = getIntent().getStringExtra("locationStr");
        setUpMapIfNeeded();

    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                final LatLng MELBOURNE = new LatLng(lat, lon);
                Marker melbourne = mMap.addMarker(new MarkerOptions()
                        .position(MELBOURNE)
                        .title(locationStr));
                melbourne.showInfoWindow();
                LatLng latLng = new LatLng(lat, lon);
                CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
                mMap.moveCamera(update);
            }
        }
    }
}