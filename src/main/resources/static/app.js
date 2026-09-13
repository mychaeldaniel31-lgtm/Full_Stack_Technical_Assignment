const $ = id => document.getElementById(id);
let token = localStorage.getItem('token'), me, page = 0;

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
}

async function api(path, opt = {}) {
  opt.headers = {...(opt.headers || {}), 'Content-Type': 'application/json', ...(token ? {Authorization: `Bearer ${token}`} : {})};
  const response = await fetch('/api' + path, opt);
  if (response.status === 401) {
    token = null;
    localStorage.removeItem('token');
    $('login').hidden = false;
    $('app').hidden = true;
    $('loginError').textContent = 'Sesi tidak valid. Silakan login kembali.';
  }
  if (!response.ok) {
    const error = await response.json().catch(() => ({message: response.statusText}));
    throw new Error(error.message || 'Request gagal');
  }
  return response.json();
}

function showApp() {
  $('login').hidden = true;
  $('app').hidden = false;
  $('who').textContent = `${me.name} · ${me.preferredTimezone}`;
  page = 0;
  load();
}

async function load() {
  $('list').textContent = 'Memuat...';
  $('previous').disabled = $('next').disabled = true;
  try {
    const rows = await api(`/appointments?page=${page}&size=50`);
    $('list').innerHTML = rows.length ? rows.map(a => `<div class="item"><strong>${escapeHtml(a.title)}</strong><div>${format(a.start)} – ${format(a.end)}</div><div class="muted">Pembuat: ${escapeHtml(a.creator)} · Undangan: ${escapeHtml(a.invitees.join(', ') || '—')}</div></div>`).join('') : '<p class="muted">Belum ada janji temu mendatang pada halaman ini.</p>';
    $('next').disabled = rows.length < 50;
  } catch (error) {
    $('list').textContent = error.message;
  } finally {
    $('previous').disabled = page === 0;
    $('page').textContent = `Halaman ${page + 1}`;
  }
}

function format(value) {
  return new Date(value).toLocaleString('id-ID', {dateStyle: 'medium', timeStyle: 'short', timeZone: me.preferredTimezone}) + ' (' + me.preferredTimezone + ')';
}

$('previous').onclick = () => { if (page > 0) { page--; load(); } };
$('next').onclick = () => { page++; load(); };
$('loginForm').onsubmit = async event => {
  event.preventDefault();
  $('loginError').textContent = '';
  try {
    const result = await api('/auth/login', {method: 'POST', body: JSON.stringify({username: $('username').value})});
    token = result.token;
    me = result.user;
    localStorage.setItem('token', token);
    showApp();
  } catch (error) {
    $('loginError').textContent = error.message;
  }
};

$('appointmentForm').onsubmit = async event => {
  event.preventDefault();
  $('formMessage').textContent = '';
  try {
    await api('/appointments', {method: 'POST', body: JSON.stringify({
      title: $('title').value,
      start: new Date($('start').value + 'Z').toISOString(),
      end: new Date($('end').value + 'Z').toISOString(),
      invitees: $('invitees').value.split(',').map(value => value.trim()).filter(Boolean)
    })});
    event.target.reset();
    $('formMessage').className = 'success';
    $('formMessage').textContent = 'Janji temu berhasil dibuat.';
    page = 0;
    load();
  } catch (error) {
    $('formMessage').className = 'error';
    $('formMessage').textContent = error.message;
  }
};

$('logout').onclick = async () => {
  try {
    await api('/auth/logout', {method: 'POST'});
    token = null;
    localStorage.removeItem('token');
    location.reload();
  } catch (error) {
    $('formMessage').className = 'error';
    $('formMessage').textContent = error.message;
  }
};
if (token) api('/me').then(user => { me = user; showApp(); }).catch(error => {
  $('loginError').textContent = error.message;
});
