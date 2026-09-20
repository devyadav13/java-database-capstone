/* Thin wrapper around the REST API. Every call resolves to {ok, status, data}. */
const Api = (() => {
  async function request(method, url, body) {
    try {
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: body ? JSON.stringify(body) : undefined,
      });
      let data = {};
      try { data = await res.json(); } catch (e) { /* empty body */ }
      return { ok: res.ok, status: res.status, data };
    } catch (e) {
      return { ok: false, status: 0, data: { message: 'Cannot reach the server. Check your connection and try again.' } };
    }
  }
  const seg = (v) => (v === undefined || v === null || String(v).trim() === '' ? 'null' : encodeURIComponent(String(v).trim()));

  return {
    adminLogin: (identifier, password) => request('POST', '/admin/login', { identifier, password }),
    doctorLogin: (identifier, password) => request('POST', '/doctor/login', { identifier, password }),
    patientLogin: (identifier, password) => request('POST', '/patient/login', { identifier, password }),
    patientSignup: (patient) => request('POST', '/patient', patient),

    getDoctors: () => request('GET', '/doctor'),
    filterDoctors: (name, time, specialty) => request('GET', `/doctor/filter/${seg(name)}/${seg(time)}/${seg(specialty)}`),
    addDoctor: (doctor, token) => request('POST', `/doctor/${token}`, doctor),
    deleteDoctor: (id, token) => request('DELETE', `/doctor/${id}/${token}`),
    getAvailability: (user, doctorId, date, token) => request('GET', `/doctor/availability/${user}/${doctorId}/${date}/${token}`),

    getDoctorAppointments: (date, patientName, token) => request('GET', `/appointments/${date}/${seg(patientName)}/${token}`),
    bookAppointment: (appointment, token) => request('POST', `/appointments/${token}`, appointment),
    cancelAppointment: (id, token) => request('DELETE', `/appointments/${id}/${token}`),
    completeAppointment: (id, token) => request('PUT', `/appointments/complete/${id}/${token}`),

    getPatient: (token) => request('GET', `/patient/${token}`),
    getPatientAppointments: (id, token) => request('GET', `/patient/${id}/${token}`),
    filterPatientAppointments: (condition, doctorName, id, token) =>
      request('GET', `/patient/filter/${seg(condition)}/${seg(doctorName)}/${id}/${token}`),

    savePrescription: (prescription, token) => request('POST', `/prescription/${token}`, prescription),
  };
})();
