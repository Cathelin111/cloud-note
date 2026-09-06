package com.cloudnote.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudnote.common.BizException;
import com.cloudnote.dto.LoginRequest;
import com.cloudnote.dto.RegisterRequest;
import com.cloudnote.entity.User;
import com.cloudnote.mapper.UserMapper;
import com.cloudnote.security.JwtUtils;
import com.cloudnote.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(UserMapper userMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String name = request.getUsername().trim();
        if (userMapper.findByName(name) != null) {
            throw new BizException(1, "用户名已被占用");
        }
        User user = new User();
        user.setCn_user_id(UUID.randomUUID().toString().replace("-", ""));
        user.setCn_user_name(name);
        // 新账号统一 BCrypt
        user.setCn_user_password(JwtUtils.bcrypt(request.getPassword()));
        String nick = request.getNick();
        user.setCn_user_nick((nick == null || nick.trim().isEmpty()) ? name : nick.trim());
        user.setCn_user_role("user");
        user.setCn_user_status("normal");
        user.setCn_user_create_time(System.currentTimeMillis());
        userMapper.insert(user);
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        String name = request.getUsername().trim();
        String rawPwd = request.getPassword();
        User user = userMapper.findByName(name);
        if (user == null) {
            throw new BizException(1, "用户名不存在");
        }
        if ("disabled".equals(user.getCn_user_status())) {
            throw new BizException(3, "该账号已被管理员停用, 请联系系统管理员");
        }
        String stored = user.getCn_user_password();
        boolean ok;
        if (JwtUtils.isBcrypt(stored)) {
            ok = JwtUtils.matchesBcrypt(rawPwd, stored);
        } else {
            // 存量旧密码: base64(md5(raw)); 校验通过后迁移为 BCrypt
            ok = stored != null && stored.equals(JwtUtils.legacyMd5Base64(rawPwd));
            if (ok) {
                userMapper.updatePassword(user.getCn_user_id(), JwtUtils.bcrypt(rawPwd));
            }
        }
        if (!ok) {
            throw new BizException(2, "密码错误");
        }
        String token = jwtUtils.generateToken(user.getCn_user_name());

        Map<String, Object> userInfo = new HashMap<String, Object>();
        userInfo.put("cn_user_id", user.getCn_user_id());
        userInfo.put("cn_user_name", user.getCn_user_name());
        userInfo.put("cn_user_nick", user.getCn_user_nick());
        userInfo.put("cn_user_role", user.getCn_user_role());
        userInfo.put("cn_user_status", user.getCn_user_status());

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("token", token);
        data.put("user", userInfo);
        return data;
    }
}
