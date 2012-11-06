package org.qii.weiciyuan.dao.group;

/**
 * User: qii
 * Date: 12-11-6
 */
public class ModifyGroupMemberDao {

    public void add() {

    }

    public void delete() {

    }

    public ModifyGroupMemberDao(String token, String uid, String list_id) {
        this.access_token = token;
        this.uid = uid;
        this.list_id = list_id;
    }

    private String access_token;
    private String uid;
    private String list_id;
}
