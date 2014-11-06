package org.qii.weiciyuan.bean;

import org.qii.weiciyuan.support.utils.ObjectToStringUtility;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User: qii
 * Date: 12-7-31
 * "geo":{"type":"Point","coordinates":[30.1953,120.199235]}
 */
public class GeoBean implements Parcelable {
    private String type;
    private double[] coordinates = {0.0, 0.0};

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public double getLat() {
        return coordinates[0];
    }

    public double getLon() {
        return coordinates[1];
    }

    public void setLatitude(double lat) {
        coordinates[0] = lat;
    }

    public void setLongitude(double lon) {
        coordinates[1] = lon;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeDoubleArray(coordinates);
    }

    public static final Parcelable.Creator<GeoBean> CREATOR =
            new Parcelable.Creator<GeoBean>() {
                public GeoBean createFromParcel(Parcel in) {
                    GeoBean geoBean = new GeoBean();
                    geoBean.type = in.readString();
                    geoBean.coordinates = new double[2];
                    in.readDoubleArray(geoBean.coordinates);
                    return geoBean;
                }

                public GeoBean[] newArray(int size) {
                    return new GeoBean[size];
                }
            };
}
