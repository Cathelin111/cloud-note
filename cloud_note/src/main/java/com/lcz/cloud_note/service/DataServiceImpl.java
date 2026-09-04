package com.lcz.cloud_note.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lcz.cloud_note.dao.ActivityDao;
import com.lcz.cloud_note.dao.BookDao;
import com.lcz.cloud_note.dao.NoteDao;
import com.lcz.cloud_note.dao.ShareDao;
import com.lcz.cloud_note.dao.UserDao;
import com.lcz.cloud_note.entity.Book;
import com.lcz.cloud_note.entity.Note;
import com.lcz.cloud_note.entity.NoteActivity;
import com.lcz.cloud_note.entity.Share;
import com.lcz.cloud_note.entity.User;
import com.lcz.cloud_note.util.NoteResult;

@Service("dataService")
@Transactional(readOnly = true)
public class DataServiceImpl implements DataService {
	//导出格式版本(导入端按版本兼容)
	private static final int EXPORT_VERSION = 1;

	@Resource
	private UserDao userDao;
	@Resource
	private BookDao bookDao;
	@Resource
	private NoteDao noteDao;
	@Resource
	private ShareDao shareDao;
	@Resource
	private ActivityDao activityDao;

	public NoteResult<Map<String,Object>> exportUserData(String userId) {
		NoteResult<Map<String,Object>> result = new NoteResult<Map<String,Object>>();
		if (userId == null || userId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		User user = userDao.findById(userId);
		if (user == null) {
			result.setStatus(2);
			result.setMsg("用户不存在");
			return result;
		}
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("exportVersion", EXPORT_VERSION);
		data.put("exportedAt", System.currentTimeMillis());
		//用户档案(不含密码)
		Map<String,Object> userInfo = new HashMap<String,Object>();
		userInfo.put("cn_user_id", user.getCn_user_id());
		userInfo.put("cn_user_name", user.getCn_user_name());
		userInfo.put("cn_user_nick", user.getCn_user_nick());
		userInfo.put("cn_user_role", user.getCn_user_role());
		userInfo.put("cn_user_status", user.getCn_user_status());
		userInfo.put("cn_user_create_time", user.getCn_user_create_time());
		data.put("user", userInfo);

		//笔记本及其中的笔记
		List<Map<String,Object>> booksOut = new ArrayList<Map<String,Object>>();
		List<String> noteIds = new ArrayList<String>();
		List<Book> books = bookDao.findByUserId(userId);
		if (books != null) {
			for (Book book : books) {
				Map<String,Object> bookOut = new HashMap<String,Object>();
				bookOut.put("cn_notebook_id", book.getCn_notebook_id());
				bookOut.put("cn_notebook_type_id", book.getCn_notebook_type_id());
				bookOut.put("cn_notebook_name", book.getCn_notebook_name());
				bookOut.put("cn_notebook_desc", book.getCn_notebook_desc());
				bookOut.put("cn_notebook_createtime", book.getCn_notebook_createtime());
				//该笔记本下笔记(取全量字段)
				List<Note> listNotes = noteDao.findByBookId(book.getCn_notebook_id());
				List<Map<String,Object>> notesOut = new ArrayList<Map<String,Object>>();
				if (listNotes != null) {
					for (Note brief : listNotes) {
						Note full = noteDao.findByNoteId(brief.getCn_note_id());
						if (full != null) {
							notesOut.add(noteToMap(full));
							noteIds.add(full.getCn_note_id());
						}
					}
				}
				bookOut.put("notes", notesOut);
				booksOut.add(bookOut);
			}
		}
		data.put("books", booksOut);

		//我的分享(引用我笔记的分享记录)
		List<Map<String,Object>> sharesOut = new ArrayList<Map<String,Object>>();
		if (!noteIds.isEmpty()) {
			List<Share> shares = shareDao.findByNoteIds(noteIds);
			if (shares != null) {
				for (Share s : shares) {
					Map<String,Object> m = new HashMap<String,Object>();
					m.put("cn_share_id", s.getCn_share_id());
					m.put("cn_share_title", s.getCn_share_title());
					m.put("cn_share_body", s.getCn_share_body());
					m.put("cn_note_id", s.getCn_note_id());
					m.put("cn_share_status", s.getCn_share_status());
					sharesOut.add(m);
				}
			}
			//我的活动投稿
			List<Map<String,Object>> subsOut = new ArrayList<Map<String,Object>>();
			List<NoteActivity> subs = activityDao.findNoteActivityByNoteIds(noteIds);
			if (subs != null) {
				for (NoteActivity na : subs) {
					Map<String,Object> m = new HashMap<String,Object>();
					m.put("cn_note_activity_id", na.getCn_note_activity_id());
					m.put("cn_activity_id", na.getCn_activity_id());
					m.put("cn_note_id", na.getCn_note_id());
					m.put("cn_note_activity_up", na.getCn_note_activity_up());
					m.put("cn_note_activity_down", na.getCn_note_activity_down());
					m.put("cn_note_activity_title", na.getCn_note_activity_title());
					m.put("cn_note_activity_body", na.getCn_note_activity_body());
					subsOut.add(m);
				}
			}
			data.put("noteActivities", subsOut);
		} else {
			data.put("noteActivities", new ArrayList<Object>());
		}
		data.put("shares", sharesOut);

		result.setStatus(0);
		result.setMsg("导出成功");
		result.setData(data);
		return result;
	}

	private Map<String,Object> noteToMap(Note note) {
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("cn_note_id", note.getCn_note_id());
		m.put("cn_notebook_id", note.getCn_notebook_id());
		m.put("cn_note_status_id", note.getCn_note_status_id());
		m.put("cn_note_type_id", note.getCn_note_type_id());
		m.put("cn_note_title", note.getCn_note_title());
		m.put("cn_note_body", note.getCn_note_body());
		m.put("cn_note_create_time", note.getCn_note_create_time());
		m.put("cn_note_last_modify_time", note.getCn_note_last_modify_time());
		return m;
	}
}
