package com.cloudnote.service.impl;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudnote.common.BizException;
import com.cloudnote.entity.Activity;
import com.cloudnote.entity.Book;
import com.cloudnote.entity.Note;
import com.cloudnote.entity.Share;
import com.cloudnote.entity.User;
import com.cloudnote.mapper.ActivityMapper;
import com.cloudnote.mapper.NoteMapper;
import com.cloudnote.mapper.NotebookMapper;
import com.cloudnote.mapper.ShareMapper;
import com.cloudnote.mapper.UserMapper;
import com.cloudnote.security.JwtUtils;
import com.cloudnote.service.AdminService;
import com.cloudnote.service.CurrentUserService;

@Service
public class AdminServiceImpl implements AdminService {

    private static final int PAGE_SIZE = 10;

    private final UserMapper userMapper;
    private final ShareMapper shareMapper;
    private final ActivityMapper activityMapper;
    private final NotebookMapper notebookMapper;
    private final NoteMapper noteMapper;
    private final CurrentUserService currentUserService;

    @Value("${backup.dir:./backup}")
    private String backupDir;

    @Value("${backup.mysqldump:}")
    private String mysqldump;

    @Value("${spring.datasource.username:root}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    public AdminServiceImpl(UserMapper userMapper, ShareMapper shareMapper, ActivityMapper activityMapper,
                            NotebookMapper notebookMapper, NoteMapper noteMapper,
                            CurrentUserService currentUserService) {
        this.userMapper = userMapper;
        this.shareMapper = shareMapper;
        this.activityMapper = activityMapper;
        this.notebookMapper = notebookMapper;
        this.noteMapper = noteMapper;
        this.currentUserService = currentUserService;
    }

    private void requireAdmin() {
        User user = currentUserService.requireUser();
        if (!"admin".equals(user.getCn_user_role())) {
            throw new BizException(403, "无管理员权限");
        }
    }

    //==================== 用户管理 ====================

    @Override
    public Map<String, Object> userPage(String keyword, Integer page) {
        requireAdmin();
        String kw = (keyword == null) ? "" : keyword.trim();
        String like = kw.isEmpty() ? "" : "%" + kw + "%";
        int p = (page == null || page < 1) ? 1 : page;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("total", userMapper.countAdmin(like));
        data.put("page", p);
        data.put("size", PAGE_SIZE);
        data.put("rows", userMapper.findAdminPage(like, (p - 1) * PAGE_SIZE, PAGE_SIZE));
        return data;
    }

    @Override
    public void updateUserStatus(String targetId, String status) {
        requireAdmin();
        User target = mustUser(targetId);
        if ("admin".equals(target.getCn_user_role())) {
            throw new BizException(3, "不能停用/启用管理员账号");
        }
        userMapper.updateStatus(targetId, status);
    }

    @Override
    @Transactional
    public void deleteUser(String targetId) {
        requireAdmin();
        User target = mustUser(targetId);
        if ("admin".equals(target.getCn_user_role())) {
            throw new BizException(3, "不能删除管理员账号");
        }
        List<String> noteIds = new ArrayList<String>();
        List<Book> books = notebookMapper.findAllByUserId(targetId);
        if (books != null) {
            for (Book book : books) {
                List<Note> notes = noteMapper.findByBookId(book.getCn_notebook_id());
                if (notes != null) {
                    for (Note note : notes) {
                        noteIds.add(note.getCn_note_id());
                    }
                }
                notebookMapper.deleteById(book.getCn_notebook_id());
            }
        }
        if (!noteIds.isEmpty()) {
            shareMapper.deleteByNoteIds(noteIds);
            activityMapper.deleteSubmissionByNoteIds(noteIds);
            noteMapper.deleteByNoteIds(noteIds);
        }
        userMapper.deleteById(targetId);
    }

    @Override
    public void promoteUser(String targetId) {
        requireAdmin();
        User target = mustUser(targetId);
        if ("admin".equals(target.getCn_user_role())) {
            throw new BizException(3, "该账号已是管理员");
        }
        userMapper.updateRole(targetId, "admin");
        userMapper.updateStatus(targetId, "normal");
    }

    //==================== 管理员管理 ====================

    @Override
    @Transactional
    public User createAdmin(String username, String password, String nick) {
        requireAdmin();
        String name = username.trim();
        if (userMapper.findByName(name) != null) {
            throw new BizException(1, "用户名已被占用");
        }
        User user = new User();
        user.setCn_user_id(UUID.randomUUID().toString().replace("-", ""));
        user.setCn_user_name(name);
        user.setCn_user_password(JwtUtils.bcrypt(password));
        user.setCn_user_nick((nick == null || nick.trim().isEmpty()) ? "管理员" : nick.trim());
        user.setCn_user_role("admin");
        user.setCn_user_status("normal");
        user.setCn_user_create_time(System.currentTimeMillis());
        userMapper.insert(user);
        return user;
    }

    @Override
    public List<User> listAdmins() {
        requireAdmin();
        return userMapper.findAdmins();
    }

    //==================== 分享管理 ====================

    @Override
    public Map<String, Object> sharePage(String keyword, Integer page) {
        requireAdmin();
        String kw = (keyword == null) ? "" : keyword.trim();
        int p = (page == null || page < 1) ? 1 : page;
        String like = kw.isEmpty() ? "" : "%" + kw + "%";
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("total", shareMapper.countAdmin(like));
        data.put("page", p);
        data.put("size", PAGE_SIZE);
        data.put("rows", shareMapper.findAdminPage(like, (p - 1) * PAGE_SIZE, PAGE_SIZE));
        return data;
    }

    @Override
    public void updateShareStatus(String shareId, String status) {
        requireAdmin();
        Share share = shareMapper.findById(shareId);
        if (share == null) {
            throw new BizException(2, "分享不存在");
        }
        shareMapper.updateStatus(shareId, status);
    }

    @Override
    public void deleteShare(String shareId) {
        requireAdmin();
        if (shareMapper.deleteById(shareId) == 0) {
            throw new BizException(2, "分享不存在");
        }
    }

    //==================== 活动管理 ====================

    @Override
    public List<Activity> listActivities() {
        requireAdmin();
        return activityMapper.findAll();
    }

    @Override
    @Transactional
    public Activity saveActivity(String activityId, String title, String body, Long endTime) {
        requireAdmin();
        Activity activity = new Activity();
        activity.setCn_activity_title(title.trim());
        activity.setCn_activity_body(body);
        activity.setCn_activity_end_time(endTime);
        if (activityId == null || activityId.trim().isEmpty()) {
            activity.setCn_activity_id(UUID.randomUUID().toString().replace("-", ""));
            activityMapper.insertActivity(activity);
        } else {
            activity.setCn_activity_id(activityId.trim());
            if (activityMapper.updateActivity(activity) == 0) {
                throw new BizException(2, "活动不存在");
            }
        }
        return activity;
    }

    @Override
    @Transactional
    public void deleteActivity(String activityId) {
        requireAdmin();
        if (activityMapper.deleteActivityById(activityId) == 0) {
            throw new BizException(2, "活动不存在");
        }
        activityMapper.deleteSubmissionByActivityId(activityId);
    }

    //==================== 备份 ====================

    @Override
    public String backup() {
        requireAdmin();
        if (mysqldump == null || mysqldump.trim().isEmpty()) {
            throw new BizException(1, "未配置 mysqldump 路径(backup.mysqldump)");
        }
        File dir = new File(backupDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File target = new File(dir, "cloudnote_" + stamp + ".sql");
        try {
            List<String> cmd = new ArrayList<String>();
            cmd.add(mysqldump.trim());
            cmd.add("--user=" + dbUser);
            cmd.add("--password=" + dbPassword);
            cmd.add("--host=127.0.0.1");
            cmd.add("--default-character-set=utf8mb4");
            cmd.add("--result-file=" + target.getAbsolutePath());
            cmd.add("cloudnote");
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("mysqldump 退出码=" + exitCode);
            }
            return target.getAbsolutePath();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(500, "备份失败: " + e.getMessage());
        }
    }

    private User mustUser(String targetId) {
        User target = userMapper.findById(targetId);
        if (target == null) {
            throw new BizException(2, "用户不存在");
        }
        return target;
    }
}
