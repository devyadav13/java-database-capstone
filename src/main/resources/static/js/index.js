document.getElementById('btnPatient').addEventListener('click', () => Auth.openLogin('patient'));
document.getElementById('btnDoctor').addEventListener('click', () => Auth.openLogin('doctor'));
document.getElementById('btnAdmin').addEventListener('click', () => Auth.openLogin('admin'));
document.getElementById('linkSignup').addEventListener('click', (e) => { e.preventDefault(); Auth.openSignup(); });

// Decorative week grid: 5 days x 6 hourly slots (open/booked pattern is illustrative).
(function drawWeek() {
  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
  const hours = ['09', '10', '11', '14', '15', '16'];
  const pattern = [
    'TTOTO', 'OTTOT', 'TOTTO',
    'OTOTT', 'TTOOT', 'OOTOT',
  ];
  const grid = document.getElementById('week');
  grid.innerHTML = '<span></span>' + days.map((d) => `<span class="head">${d}</span>`).join('');
  hours.forEach((h, r) => {
    grid.insertAdjacentHTML('beforeend', `<span class="time">${h}:00</span>`);
    pattern[r].split('').forEach((c) => {
      grid.insertAdjacentHTML('beforeend', `<span class="slot ${c === 'T' ? 'taken' : 'open'}"></span>`);
    });
  });
})();
