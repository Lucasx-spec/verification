import http from './http'

export const authApi = {
  register(data) {
    return http.post('/auth/register', data)
  },
  login(data) {
    return http.post('/auth/login', data)
  },
  me() {
    return http.get('/auth/me')
  },
  updateKeyPair(data) {
    return http.put('/auth/keypair', data)
  },
}

export const signApi = {
  create(data) {
    return http.post('/sign/records', data)
  },
  list() {
    return http.get('/sign/records')
  },
  adminList() {
    return http.get('/sign/admin/records')
  },
  detail(signNo) {
    return http.get(`/sign/records/${signNo}`)
  },
}

export const verifyApi = {
  createLink(data) {
    return http.post('/verify/links', data)
  },
  listLinks() {
    return http.get('/verify/links')
  },
  linkDetail(verifyToken) {
    return http.get(`/verify/links/${verifyToken}`)
  },
  disableLink(verifyToken) {
    return http.put(`/verify/links/${verifyToken}/disable`)
  },
  listRecords() {
    return http.get('/verify/records')
  },
  adminListRecords() {
    return http.get('/verify/admin/records')
  },
  verifyFile(formData) {
    return http.post('/verify/files', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },
}

export const systemApi = {
  getSettings() {
    return http.get('/system/settings')
  },
  updateSettings(data) {
    return http.put('/system/settings', data)
  },
}

export const auditApi = {
  myLogs() {
    return http.get('/audit/logs')
  },
  adminLogs() {
    return http.get('/audit/admin/logs')
  },
  integrity() {
    return http.get('/audit/integrity')
  },
}
