package org.qii.weiciyuan.bean;

import java.io.Serializable;

/**
 * User: qii
 * Date: 12-8-27
 */
public abstract class ListBean implements Serializable {

    protected int total_number = 0;

    public abstract int getSize();

    public int getTotal_number() {
        return total_number;
    }
}
