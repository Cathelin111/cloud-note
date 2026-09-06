package com.cloudnote.mapper;

import org.apache.ibatis.annotations.Param;

import com.cloudnote.entity.User;

public interface UserMapper {

    User findByName(@Param("name") String name);

    User findById(@Param("userId") String userId);

    int insert(User user);

    int updatePassword(@Param("userId") String userId, @Param("password") String password);

    int updateStatus(@Param("userId") String userId, @Param("status") String status);
}
