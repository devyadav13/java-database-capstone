/* Login and signup modals shared by the landing page and the patient page. */
const Auth = (() => {
  const roleLabels = { admin: 'Admin', doctor: 'Doctor', patient: 'Patient' };

  function inject() {
    if (document.getElementById('loginModal')) return;
    document.body.insertAdjacentHTML('beforeend', `
      <div class="modal-backdrop" id="loginModal">
        <form class="modal" id="loginForm" aria-labelledby="loginTitle">
          <h2 id="loginTitle">Log in</h2>
          <div class="field"><label for="loginId" id="loginIdLabel">Email</label><input id="loginId" required autocomplete="username"></div>
          <div class="field"><label for="loginPw">Password</label><input id="loginPw" type="password" required autocomplete="current-password"></div>
          <div class="modal-actions">
            <button type="button" class="btn secondary" data-close>Cancel</button>
            <button type="submit" class="btn">Log in</button>
          </div>
        </form>
      </div>
      <div class="modal-backdrop" id="signupModal">
        <form class="modal" id="signupForm" aria-labelledby="signupTitle">
          <h2 id="signupTitle">Create a patient account</h2>
          <div class="field"><label for="suName">Full name</label><input id="suName" required minlength="3"></div>
          <div class="field"><label for="suEmail">Email</label><input id="suEmail" type="email" required></div>
          <div class="field"><label for="suPw">Password</label><input id="suPw" type="password" required minlength="6"><span class="hint">At least 6 characters.</span></div>
          <div class="field"><label for="suPhone">Phone</label><input id="suPhone" required pattern="[0-9]{10}" inputmode="numeric"><span class="hint">10 digits, no spaces.</span></div>
          <div class="field"><label for="suAddress">Address</label><input id="suAddress" required></div>
          <div class="modal-actions">
            <button type="button" class="btn secondary" data-close>Cancel</button>
            <button type="submit" class="btn">Create account</button>
          </div>
        </form>
      </div>`);
    document.getElementById('loginForm').addEventListener('submit', onLogin);
    document.getElementById('signupForm').addEventListener('submit', onSignup);
  }

  let currentRole = 'patient';

  function openLogin(role) {
    inject();
    currentRole = role;
    document.getElementById('loginTitle').textContent = `${roleLabels[role]} log in`;
    document.getElementById('loginIdLabel').textContent = role === 'admin' ? 'Username' : 'Email';
    document.getElementById('loginId').type = role === 'admin' ? 'text' : 'email';
    document.getElementById('loginForm').reset();
    UI.closeModal('signupModal');
    UI.openModal('loginModal');
    document.getElementById('loginId').focus();
  }

  function openSignup() {
    inject();
    UI.closeModal('loginModal');
    UI.openModal('signupModal');
    document.getElementById('suName').focus();
  }

  async function onLogin(e) {
    e.preventDefault();
    const id = document.getElementById('loginId').value.trim();
    const pw = document.getElementById('loginPw').value;
    const call = { admin: Api.adminLogin, doctor: Api.doctorLogin, patient: Api.patientLogin }[currentRole];
    const res = await call(id, pw);
    if (!res.ok) { UI.toast(res.data.message || 'Login failed', true); return; }
    localStorage.setItem('token', res.data.token);
    localStorage.setItem('role', currentRole);
    if (currentRole === 'admin') window.location.href = `/adminDashboard/${res.data.token}`;
    else if (currentRole === 'doctor') window.location.href = `/doctorDashboard/${res.data.token}`;
    else window.location.href = '/pages/patientDashboard.html';
  }

  async function onSignup(e) {
    e.preventDefault();
    const patient = {
      name: document.getElementById('suName').value.trim(),
      email: document.getElementById('suEmail').value.trim(),
      password: document.getElementById('suPw').value,
      phone: document.getElementById('suPhone').value.trim(),
      address: document.getElementById('suAddress').value.trim(),
    };
    const res = await Api.patientSignup(patient);
    if (!res.ok) { UI.toast(res.data.message || 'Signup failed', true); return; }
    UI.toast('Account created. Log in to continue.');
    openLogin('patient');
    document.getElementById('loginId').value = patient.email;
  }

  return { openLogin, openSignup };
})();
