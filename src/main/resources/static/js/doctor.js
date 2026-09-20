const token = window.location.pathname.split('/').pop();
localStorage.setItem('token', token);
localStorage.setItem('role', 'doctor');

const dateEl = document.getElementById('fDate');
dateEl.value = UI.todayIso();
let rxAppointment = null;

async function loadAppointments() {
  const res = await Api.getDoctorAppointments(dateEl.value, document.getElementById('fPatient').value, token);
  const box = document.getElementById('appointmentList');
  if (!res.ok) { UI.toast(res.data.message || 'Could not load appointments', true); return; }
  const list = (res.data.appointments || []).slice().sort((a, b) => a.appointmentTime.localeCompare(b.appointmentTime));
  document.getElementById('count').textContent = `${list.length} appointment${list.length === 1 ? '' : 's'}`;
  if (!list.length) {
    box.innerHTML = '<div class="empty">No appointments on this date. Pick another date to check.</div>';
    return;
  }
  box.innerHTML = `<div class="table-wrap"><table>
    <thead><tr><th>Time</th><th>Patient</th><th>Phone</th><th>Status</th><th>Actions</th></tr></thead>
    <tbody>${list.map((a) => `
      <tr>
        <td>${UI.escapeHtml(UI.formatTime(a.appointmentTime))}</td>
        <td>${UI.escapeHtml(a.patient.name)}</td>
        <td>${UI.escapeHtml(a.patient.phone)}</td>
        <td><span class="badge ${a.status === 1 ? 'completed' : 'scheduled'}">${a.status === 1 ? 'Completed' : 'Scheduled'}</span></td>
        <td>
          <button class="btn small secondary" data-rx="${a.id}" data-name="${UI.escapeHtml(a.patient.name)}">Prescribe</button>
          ${a.status === 1 ? '' : `<button class="btn small" data-done="${a.id}">Mark completed</button>`}
        </td>
      </tr>`).join('')}</tbody></table></div>`;

  box.querySelectorAll('[data-rx]').forEach((b) => b.addEventListener('click', () => {
    rxAppointment = { id: Number(b.dataset.rx), name: b.dataset.name };
    document.getElementById('rxForm').reset();
    document.getElementById('rxFor').textContent = `For ${rxAppointment.name}`;
    UI.openModal('rxModal');
  }));
  box.querySelectorAll('[data-done]').forEach((b) => b.addEventListener('click', async () => {
    const r = await Api.completeAppointment(b.dataset.done, token);
    UI.toast(r.data.message || 'Updated', !r.ok);
    if (r.ok) loadAppointments();
  }));
}

document.getElementById('filters').addEventListener('submit', (e) => { e.preventDefault(); loadAppointments(); });
document.getElementById('btnLogout').addEventListener('click', UI.logout);

document.getElementById('rxForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const res = await Api.savePrescription({
    appointmentId: rxAppointment.id,
    patientName: rxAppointment.name,
    medication: document.getElementById('rxMed').value.trim(),
    dosage: document.getElementById('rxDose').value.trim(),
    doctorNotes: document.getElementById('rxNotes').value.trim(),
  }, token);
  UI.toast(res.data.message || (res.ok ? 'Prescription saved' : 'Could not save prescription'), !res.ok);
  if (res.ok) UI.closeModal('rxModal');
});

loadAppointments();
