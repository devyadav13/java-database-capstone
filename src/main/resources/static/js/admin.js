const token = window.location.pathname.split('/').pop();
localStorage.setItem('token', token);
localStorage.setItem('role', 'admin');

const SLOTS = ['09:00-10:00', '10:00-11:00', '11:00-12:00', '14:00-15:00', '15:00-16:00', '16:00-17:00'];
const listEl = document.getElementById('doctorList');

document.getElementById('slotChecks').innerHTML = SLOTS
  .map((s) => `<label><input type="checkbox" value="${s}"> ${s}</label>`).join('');

async function loadDoctors() {
  const name = document.getElementById('fName').value;
  const specialty = document.getElementById('fSpecialty').value;
  const time = document.getElementById('fTime').value;
  const res = await Api.filterDoctors(name, time, specialty);
  if (!res.ok) { UI.toast(res.data.message || 'Could not load doctors', true); return; }
  const doctors = res.data.doctors || [];
  document.getElementById('count').textContent = `${doctors.length} doctor${doctors.length === 1 ? '' : 's'}`;
  UI.renderDoctors(listEl, doctors, [{ label: 'Delete', cls: 'danger', onClick: deleteDoctor }]);
}

async function deleteDoctor(doctor) {
  if (!confirm(`Delete ${doctor.name}? Their appointments will also be removed.`)) return;
  const res = await Api.deleteDoctor(doctor.id, token);
  UI.toast(res.data.message || (res.ok ? 'Doctor deleted' : 'Delete failed'), !res.ok);
  if (res.ok) loadDoctors();
}

document.getElementById('filters').addEventListener('submit', (e) => { e.preventDefault(); loadDoctors(); });
document.getElementById('btnAddDoctor').addEventListener('click', () => {
  document.getElementById('addDoctorForm').reset();
  UI.openModal('addDoctorModal');
});
document.getElementById('btnLogout').addEventListener('click', UI.logout);

document.getElementById('addDoctorForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const availableTimes = [...document.querySelectorAll('#slotChecks input:checked')].map((c) => c.value);
  if (!availableTimes.length) { UI.toast('Select at least one available slot', true); return; }
  const doctor = {
    name: document.getElementById('dName').value.trim(),
    specialty: document.getElementById('dSpecialty').value.trim(),
    email: document.getElementById('dEmail').value.trim(),
    password: document.getElementById('dPassword').value,
    phone: document.getElementById('dPhone').value.trim(),
    availableTimes,
  };
  const res = await Api.addDoctor(doctor, token);
  UI.toast(res.data.message || (res.ok ? 'Doctor added' : 'Could not add doctor'), !res.ok);
  if (res.ok) { UI.closeModal('addDoctorModal'); loadDoctors(); }
});

loadDoctors();
