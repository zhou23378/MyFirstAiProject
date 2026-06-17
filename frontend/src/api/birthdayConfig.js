import request from './request'

export const birthdayConfigApi = {
    get: () => request.get('/api/birthday-config'),
    update: (data) => request.put('/api/birthday-config', data),
}
