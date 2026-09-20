/* Shared UI helpers: toast, modals, formatting and doctor cards. */
const UI = (() => {
  const escapeHtml = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

  let toastTimer;
  function toast(message, isError = false) {
    let el = document.getElementById('toast');
    if (!el) { el = document.createElement('div'); el.id = 'toast'; el.setAttribute('role', 'status'); document.body.appendChild(el); }
    el.textContent = message;
    el.className = 'show' + (isError ? ' error' : '');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { el.className = ''; }, 3500);
  }

  const openModal = (id) => document.getElementById(id).classList.add('open');
  const closeModal = (id) => document.getElementById(id).classList.remove('open');

  function formatDateTime(iso) {
    const d = new Date(iso);
    return d.toLocaleString(undefined, { weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
  }
  const formatTime = (iso) => new Date(iso).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
  const todayIso = () => new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);

  /** actions: [{label, cls, onClick(doctor)}] */
  function doctorCard(doctor, actions = []) {
    const card = document.createElement('article');
    card.className = 'doctor-card';
    const slots = (doctor.availableTimes || []).slice().sort().map((s) => `<span class="chip">${escapeHtml(s)}</span>`).join('');
    card.innerHTML = `
      <h3>${escapeHtml(doctor.name)}</h3>
      <div class="specialty">${escapeHtml(doctor.specialty)}</div>
      <div class="meta">${escapeHtml(doctor.email)}<br>${escapeHtml(doctor.phone)}</div>
      <div class="chips" aria-label="Available time slots">${slots || '<span class="hint">No slots set</span>'}</div>
      <div class="card-actions"></div>`;
    const box = card.querySelector('.card-actions');
    actions.forEach((a) => {
      const b = document.createElement('button');
      b.className = 'btn small ' + (a.cls || '');
      b.textContent = a.label;
      b.addEventListener('click', () => a.onClick(doctor));
      box.appendChild(b);
    });
    return card;
  }

  function renderDoctors(container, doctors, actions) {
    container.innerHTML = '';
    if (!doctors.length) {
      container.innerHTML = '<div class="empty">No doctors match these filters. Clear a filter and search again.</div>';
      return;
    }
    doctors.forEach((d) => container.appendChild(doctorCard(d, actions)));
  }

  function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    window.location.href = '/';
  }

  // Close a modal when the backdrop or a [data-close] button is clicked.
  document.addEventListener('click', (e) => {
    if (e.target.classList && e.target.classList.contains('modal-backdrop')) e.target.classList.remove('open');
    const closer = e.target.closest && e.target.closest('[data-close]');
    if (closer) closer.closest('.modal-backdrop').classList.remove('open');
  });

  return { escapeHtml, toast, openModal, closeModal, formatDateTime, formatTime, todayIso, doctorCard, renderDoctors, logout };
})();
