import api from './api';
export const userService={list:()=>api.get('/admin/users?size=200').then(r=>r.content),create:data=>api.post('/admin/users',data),updateRoles:(id,roles)=>api.patch(`/admin/users/${id}/roles`,{roles}),updateStatus:(id,enabled)=>api.patch(`/admin/users/${id}/status`,{enabled})};
