import request from '../utils/request'

// ============ 认证 ============
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data)
}

// ============ 笔记本 ============
export const notebookApi = {
  list: () => request.get('/notebooks'),
  create: (title) => request.post('/notebooks', { title }),
  rename: (id, name) => request.put(`/notebooks/${id}`, { name }),
  remove: (id) => request.delete(`/notebooks/${id}`),
  special: () => request.get('/notebooks/special')
}

// ============ 笔记 ============
export const noteApi = {
  listByBook: (bookId) => request.get('/notes', { params: { bookId } }),
  detail: (id) => request.get(`/notes/${id}`),
  create: (bookId, title) => request.post('/notes', { bookId, title }),
  update: (id, title, body) => request.put(`/notes/${id}`, { title, body }),
  softDelete: (id) => request.delete(`/notes/${id}`),
  move: (id, bookId) => request.patch(`/notes/${id}/move`, { bookId }),
  permanent: (id) => request.delete(`/notes/${id}/permanent`)
}

// ============ 分享 ============
export const shareApi = {
  publicPage: (params) => request.get('/public/shares', { params }),
  publicDetail: (id) => request.get(`/public/shares/${id}`),
  shareMine: (noteId) => request.post('/shares', { noteId }),
  collect: (shareId) => request.post(`/shares/${shareId}/collect`)
}

// ============ 活动 ============
export const activityApi = {
  list: () => request.get('/public/activities'),
  submissions: (activityId, page) => request.get(`/public/activities/${activityId}/submissions`, { params: { page } }),
  submissionDetail: (id) => request.get(`/public/activities/submissions/${id}`),
  join: (activityId, noteId) => request.post(`/activities/${activityId}/join`, { noteId }),
  up: (id) => request.post(`/activities/submissions/${id}/up`),
  down: (id) => request.post(`/activities/submissions/${id}/down`),
  collectSubmission: (id) => request.post(`/activities/submissions/${id}/collect`)
}

// ============ 管理员 ============
export const adminApi = {
  users: (params) => request.get('/admin/users', { params }),
  userStatus: (id, status) => request.patch(`/admin/users/${id}/status`, { status }),
  promote: (id) => request.put(`/admin/users/${id}/promote`),
  deleteUser: (id) => request.delete(`/admin/users/${id}`),
  admins: () => request.get('/admin/admins'),
  createAdmin: (data) => request.post('/admin/admins', data),
  shares: (params) => request.get('/admin/shares', { params }),
  shareStatus: (id, status) => request.patch(`/admin/shares/${id}/status`, { status }),
  deleteShare: (id) => request.delete(`/admin/shares/${id}`),
  activities: () => request.get('/admin/activities'),
  saveActivity: (data) => request.post('/admin/activities', data),
  deleteActivity: (id) => request.delete(`/admin/activities/${id}`),
  backup: () => request.post('/admin/system/backup')
}
