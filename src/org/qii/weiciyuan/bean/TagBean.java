package org.qii.weiciyuan.bean;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-8-5
 * Time: 下午5:59
 * To change this template use File | Settings | File Templates.
 */

/**
 * not defined by sina weibo
 */
public class TagBean {

    private int id;
    private String name;

    private String weight;

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
