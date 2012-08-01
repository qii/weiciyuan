package org.qii.weiciyuan.bean;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-31
 * Time: 下午8:14
 * To change this template use File | Settings | File Templates.
 */
public class Geo implements Serializable {
    private String type;
    private String[] coordinates;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String[] coordinates) {
        this.coordinates = coordinates;
    }
}
