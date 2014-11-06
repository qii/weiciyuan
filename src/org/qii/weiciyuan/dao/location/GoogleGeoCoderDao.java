package org.qii.weiciyuan.dao.location;

import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.support.utils.Utility;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GoogleGeoCoderDao {

    public String get() {
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());

        List<Address> addresses = null;
        try {
            if (!Utility.isGPSLocationCorrect(geoBean)) {
                return null;
            }
            addresses = geocoder.getFromLocation(geoBean.getLat(), geoBean.getLon(), 1);
        } catch (IOException e) {

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
        return null;
    }

    public GoogleGeoCoderDao(Activity activity, GeoBean geoBean) {
        this.activity = activity;
        this.geoBean = geoBean;
    }

    private Activity activity;
    private GeoBean geoBean;
}