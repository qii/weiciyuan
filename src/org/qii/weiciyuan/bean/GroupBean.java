package org.qii.weiciyuan.bean;

import java.io.Serializable;

/**
 * User: qii
 * Date: 12-10-17
 */
public class GroupBean implements Serializable {

    private String id;
    private String idstr;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdstr() {
        return idstr;
    }

    public void setIdstr(String idstr) {
        this.idstr = idstr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return getName();
    }
}
