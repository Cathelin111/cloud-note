package com.cloudnote.entity;

/**
 * 用户实体, 对应 cn_user(与旧系统一致, 便于数据与语义迁移)
 */
public class User {

    private String cn_user_id;
    private String cn_user_name;
    private String cn_user_password;
    private String cn_user_token;
    private String cn_user_nick;
    //角色: user=普通用户 admin=系统管理员
    private String cn_user_role;
    //状态: normal=正常 disabled=停用
    private String cn_user_status;
    //注册时间(毫秒时间戳)
    private Long cn_user_create_time;

    public String getCn_user_id() {
        return cn_user_id;
    }

    public void setCn_user_id(String cn_user_id) {
        this.cn_user_id = cn_user_id;
    }

    public String getCn_user_name() {
        return cn_user_name;
    }

    public void setCn_user_name(String cn_user_name) {
        this.cn_user_name = cn_user_name;
    }

    public String getCn_user_password() {
        return cn_user_password;
    }

    public void setCn_user_password(String cn_user_password) {
        this.cn_user_password = cn_user_password;
    }

    public String getCn_user_token() {
        return cn_user_token;
    }

    public void setCn_user_token(String cn_user_token) {
        this.cn_user_token = cn_user_token;
    }

    public String getCn_user_nick() {
        return cn_user_nick;
    }

    public void setCn_user_nick(String cn_user_nick) {
        this.cn_user_nick = cn_user_nick;
    }

    public String getCn_user_role() {
        return cn_user_role;
    }

    public void setCn_user_role(String cn_user_role) {
        this.cn_user_role = cn_user_role;
    }

    public String getCn_user_status() {
        return cn_user_status;
    }

    public void setCn_user_status(String cn_user_status) {
        this.cn_user_status = cn_user_status;
    }

    public Long getCn_user_create_time() {
        return cn_user_create_time;
    }

    public void setCn_user_create_time(Long cn_user_create_time) {
        this.cn_user_create_time = cn_user_create_time;
    }
}
