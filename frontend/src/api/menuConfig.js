import request from './request'

export const menuConfigApi = {
  list: () => request.get('/api/menu-config'),
  save: (data) => request.put('/api/menu-config', data),
}
