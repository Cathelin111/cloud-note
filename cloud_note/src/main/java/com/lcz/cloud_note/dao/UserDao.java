package com.lcz.cloud_note.dao;

import java.util.List;
import java.util.Map;

import com.lcz.cloud_note.entity.User;

public interface UserDao {
	//查找登录的账户名  返回查找到的用户名对象(登录方法)
	public User findByName(String name);
	//按用户ID查询用户
	public User findById(String userId);
	//保存一个用户名，输入类型为User(注册方法)
	public void save(User user);
	//修改用户名密码的操作
	public void change(User user);
	//后台: 分页查询普通用户列表(按用户名/昵称模糊查询, 排除管理员)
	public List<User> findAdminPage(Map<String,Object> params);
	//后台: 统计符合条件的普通用户数量
	public int countAdmin(Map<String,Object> params);
	//后台: 修改用户状态(停用/启用)
	public int updateStatus(Map<String,Object> params);
	//后台: 删除用户
	public int deleteById(String userId);
}
