import api from './api';
const payload=d=>({name:d.name,licenseNumber:d.licenseNumber,licenseCategory:d.licenseCategory,licenseExpiryDate:d.licenseExpiryDate,contactNumber:d.contactNumber,email:d.email||null,region:d.region,safetyScore:Number(d.safetyScore)});
const updatePayload=d=>{const p=payload(d);delete p.licenseNumber;return p};
export const driverService={
 getAll:()=>api.get('/drivers?size=200').then(r=>r.content),getById:id=>api.get(`/drivers/${id}`),getAvailable:()=>api.get('/drivers/available'),
 create:d=>api.post('/drivers',payload(d)),
 update:async(id,d)=>{const v=await api.put(`/drivers/${id}`,updatePayload(d));if(d.status&&d.status!==v.status)await api.patch(`/drivers/${id}/status`,{status:d.status});return api.get(`/drivers/${id}`)},
 changeStatus:(id,status)=>api.patch(`/drivers/${id}/status`,{status}),delete:id=>api.delete(`/drivers/${id}`),
};
