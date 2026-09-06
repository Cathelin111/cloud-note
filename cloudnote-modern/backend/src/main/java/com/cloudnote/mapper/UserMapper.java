package com.cloudnote.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cloudnote.entity.User;

public interface UserMapper {

    User findByName(@Param("name") String name);

    User findById(@Param("userId") String userId);

    int insert(User user);

    int updatePassword(@Param("userId") String userId, @Param("password") String password);

    int updateStatus(@Param("userId") String userId, @Param("status") String status);

    //================= 管理员后台 =================

    /** 普通用户分页(排除admin, 用户名/昵称模糊) */
    List<User> findAdminPage(@Param("keyword") String keyword,
                             @Param("begin") int begin,
                             @Param("pageSize") int pageSize);

    int countAdmin(@Param("keyword") String keyword);

    int updateRole(@Param("userId") String userId, @Param("role") String role);

    List<User> findAdmins();

    int deleteById(@Param("userId") String userId);
}
