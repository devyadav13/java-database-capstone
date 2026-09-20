const token = localStorage.getItem('role') === 'patient' ? localStorage.getItem('token') : null;
let patient = null;
let bookingDoctor = null;
let chosenSlot = null;

const listEl = document.getElementById('doctorList');

async function init() {
  const nav = document.getElementById('nav');
  if (token) {
    const res = await Api.getPatient(token);
    if (res.ok) patient = res.data.patient;
    else { localStorage.removeItem('token'); localStorage.removeItem('role'); }
  }
  if (patient) {
    nav.innerHTML = `<span class="who">Hello, ${UI.escapeHtml(patient.name)}</span><button class="btn small secondary" id="btnLogout">Log out</button>`;
    document.getElementById('btnLogout').addEventListener('click', UI.logout);
    document.getElementById('mySection').hidden = false;
    loadMyAppointments();
  } else {
    nav.innerHTML = '<button class="btn small" id="btnLogin">Log in</button><button class="btn small secondary" id="btnSignup">Sign up</button>';
    document.getElementById('btnLogin').addEventListener('click', () => Auth.openLogin('patient'));
    document.getElementById('btnSignup').addEventListener('click', () => Auth.openSignup());
  }
  loadDoctors();
}

async function loadDoctors() {
  const res = await Api.filterDoctors(
    document.getElementById('fName').value,
    document.getElementById('fTime').value,
    document.getElementById('fSpecialty').value);
  if (!res.ok) { UI.toast(res.data.message || 'Could not load doctors', true); return; }
  const doctors = res.data.doctors || [];
  document.getElementById('count').textContent = `${doctors.length} doctor${doctors.length === 1 ? '' : 's'} found`;
  UI.renderDoctors(listEl, doctors, [{ label: 'Book now', onClick: startBooking }]);
}

function startBooking(doctor) {
  if (!patient) { UI.toast('Log in to book an appointment'); Auth.openLogin('patient'); return; }
  bookingDoctor = doctor;
  chosenSlot = null;
  document.getElementById('bookWith').textContent = `With ${doctor.name} (${doctor.specialty})`;
  const dateEl = document.getElementById('bookDate');
  dateEl.min = UI.todayIso();
  dateEl.value = '';
  document.getElementById('slotPicker').innerHTML = '<span class="hint">Choose a date to see open slots.</span>';
  UI.openModal('bookModal');
}

document.getElementById('bookDate').addEventListener('change', async (e) => {
  const picker = document.getElementById('slotPicker');
  chosenSlot = null;
  if (!e.target.value) return;
  const res = await Api.getAvailability('patient', bookingDoctor.id, e.target.value, token);
  if (!res.ok) { UI.toast(res.data.message || 'Could not load slots', true); return; }
  const slots = res.data.availability || [];
  if (!slots.length) { picker.innerHTML = '<span class="hint">No open slots on this date. Try another day.</span>'; return; }
  picker.innerHTML = slots.map((s) => `<button type="button" class="slot-btn" data-slot="${s}">${s}</button>`).join('');
  picker.querySelectorAll('.slot-btn').forEach((b) => b.addEventListener('click', () => {
    picker.querySelectorAll('.slot-btn').forEach((x) => x.classList.remove('selected'));
    b.classList.add('selected');
    chosenSlot = b.dataset.slot;
  }));
});

document.getElementById('bookForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!chosenSlot) { UI.toast('Pick an open slot first', true); return; }
  const start = chosenSlot.split('-')[0];
  const res = await Api.bookAppointment({
    doctor: { id: bookingDoctor.id },
    appointmentTime: `${document.getElementById('bookDate').value}T${start}:00`,
  }, token);
  UI.toast(res.data.message || (res.ok ? 'Booked' : 'Booking failed'), !res.ok);
  if (res.ok) { UI.closeModal('bookModal'); loadMyAppointments(); }
});

async function loadMyAppointments() {
  const condition = document.getElementById('apptFilter').value;
  const res = await Api.filterPatientAppointments(condition, '', patient.id, token);
  const box = document.getElementById('myAppointments');
  if (!res.ok) { UI.toast(res.data.message || 'Could not load appointments', true); return; }
  const list = res.data.appointments || [];
  if (!list.length) { box.innerHTML = '<div class="empty">No appointments yet. Find a doctor below and book a slot.</div>'; return; }
  box.innerHTML = `<div class="table-wrap"><table>
    <thead><tr><th>When</th><th>Doctor</th><th>Specialty</th><th>Status</th><th></th></tr></thead>
    <tbody>${list.map((a) => `
      <tr>
        <td>${UI.escapeHtml(UI.formatDateTime(a.appointmentTime))}</td>
        <td>${UI.escapeHtml(a.doctor.name)}</td>
        <td>${UI.escapeHtml(a.doctor.specialty)}</td>
        <td><span class="badge ${a.status === 1 ? 'completed' : 'scheduled'}">${a.status === 1 ? 'Completed' : 'Scheduled'}</span></td>
        <td>${a.status === 1 ? '' : `<button class="btn small danger" data-cancel="${a.id}">Cancel</button>`}</td>
      </tr>`).join('')}</tbody></table></div>`;
  box.querySelectorAll('[data-cancel]').forEach((b) => b.addEventListener('click', async () => {
    if (!confirm('Cancel this appointment?')) return;
    const r = await Api.cancelAppointment(b.dataset.cancel, token);
    UI.toast(r.data.message || 'Updated', !r.ok);
    if (r.ok) { loadMyAppointments(); }
  }));
}

document.getElementById('filters').addEventListener('submit', (e) => { e.preventDefault(); loadDoctors(); });
document.getElementById('apptFilter').addEventListener('change', loadMyAppointments);
init();
