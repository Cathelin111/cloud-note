package com.cloudnote.entity;

import java.sql.Timestamp;

/**
 * 笔记本实体, 对应 cn_notebook(与旧系统一致)
 */
public class Book {

    private String cn_notebook_id;
    private String cn_user_id;
    private String cn_notebook_type_id;
    private String cn_notebook_name;
    private String cn_notebook_desc;
    private Timestamp cn_notebook_createtime;
    //关联: 类型代码(normal/favorites/recycle/action/push)
    private String cn_notebook_type_code;

    public String getCn_notebook_id() {
        return cn_notebook_id;
    }

    public void setCn_notebook_id(String cn_notebook_id) {
        this.cn_notebook_id = cn_notebook_id;
    }

    public String getCn_user_id() {
        return cn_user_id;
    }

    public void setCn_user_id(String cn_user_id) {
        this.cn_user_id = cn_user_id;
    }

    public String getCn_notebook_type_id() {
        return cn_notebook_type_id;
    }

    public void setCn_notebook_type_id(String cn_notebook_type_id) {
        this.cn_notebook_type_id = cn_notebook_type_id;
    }

    public String getCn_notebook_name() {
        return cn_notebook_name;
    }

    public void setCn_notebook_name(String cn_notebook_name) {
        this.cn_notebook_name = cn_notebook_name;
    }

    public String getCn_notebook_desc() {
        return cn_notebook_desc;
    }

    public void setCn_notebook_desc(String cn_notebook_desc) {
        this.cn_notebook_desc = cn_notebook_desc;
    }

    public Timestamp getCn_notebook_createtime() {
        return cn_notebook_createtime;
    }

    public void setCn_notebook_createtime(Timestamp cn_notebook_createtime) {
        this.cn_notebook_createtime = cn_notebook_createtime;
    }

    public String getCn_notebook_type_code() {
        return cn_notebook_type_code;
    }

    public void setCn_notebook_type_code(String cn_notebook_type_code) {
        this.cn_notebook_type_code = cn_notebook_type_code;
    }
}
