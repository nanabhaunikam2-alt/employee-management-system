'use strict';

// Frontend-only data layer. Replace these storage operations with Java API calls later.
const STORAGE_KEY = 'peopledesk.employees.v1';
const $ = (selector) => document.querySelector(selector);
const apiMode = location.protocol === 'http:' || location.protocol === 'https:';
let csrfToken = '';
let backendReady = false;
const currentMonth = new Date().toISOString().slice(0, 7);
const sampleEmployees = [
  {id: 1001, name: 'Ananya Kapoor', email: 'ananya.k@example.com', phone: '9876543210', department: 'Engineering', designation: 'Software Developer', salary: 65000, joiningDate: '2025-06-12'},
  {id: 1002, name: 'Rohan Mehta', email: 'rohan.m@example.com', phone: '9876543211', department: 'Design', designation: 'UI/UX Designer', salary: 55000, joiningDate: '2025-08-04'},
  {id: 1003, name: 'Priya Sharma', email: 'priya.s@example.com', phone: '9876543212', department: 'Human Resources', designation: 'HR Manager', salary: 60000, joiningDate: '2024-11-18'},
  {id: 1004, name: 'Arjun Patel', email: 'arjun.p@example.com', phone: '9876543213', department: 'Engineering', designation: 'Backend Developer', salary: 70000, joiningDate: `${currentMonth}-01`},
  {id: 1005, name: 'Sneha Desai', email: 'sneha.d@example.com', phone: '9876543214', department: 'Marketing', designation: 'Marketing Executive', salary: 42000, joiningDate: '2025-09-22'},
  {id: 1006, name: 'Vikram Singh', email: 'vikram.s@example.com', phone: '9876543215', department: 'Finance', designation: 'Financial Analyst', salary: 58000, joiningDate: `${currentMonth}-01`},
  {id: 1007, name: 'Neha Joshi', email: 'neha.j@example.com', phone: '9876543216', department: 'Engineering', designation: 'QA Engineer', salary: 48000, joiningDate: '2026-03-09'},
  {id: 1008, name: 'Aditya Rao', email: 'aditya.r@example.com', phone: '9876543217', department: 'Design', designation: 'Product Designer', salary: 62000, joiningDate: '2026-01-15'},
  {id: 1009, name: 'Ishita Kulkarni', email: 'ishita.k@example.com', phone: '9876543218', department: 'Engineering', designation: 'Frontend Developer', salary: 56000, joiningDate: '2026-02-16'},
  {id: 1010, name: 'Rahul Verma', email: 'rahul.v@example.com', phone: '9876543219', department: 'Operations', designation: 'Operations Manager', salary: 68000, joiningDate: '2025-04-07'},
  {id: 1011, name: 'Kavya Nair', email: 'kavya.n@example.com', phone: '9876543220', department: 'Marketing', designation: 'Content Strategist', salary: 46000, joiningDate: '2026-05-11'},
  {id: 1012, name: 'Siddharth Shah', email: 'siddharth.s@example.com', phone: '9876543221', department: 'Finance', designation: 'Accountant', salary: 50000, joiningDate: '2025-07-21'},
  {id: 1013, name: 'Meera Iyer', email: 'meera.i@example.com', phone: '9876543222', department: 'Human Resources', designation: 'Recruitment Specialist', salary: 44000, joiningDate: '2026-06-08'},
  {id: 1014, name: 'Dev Malhotra', email: 'dev.m@example.com', phone: '9876543223', department: 'Engineering', designation: 'DevOps Engineer', salary: 74000, joiningDate: '2025-12-01'},
  {id: 1015, name: 'Aditi Patil', email: 'aditi.p@example.com', phone: '9876543224', department: 'Design', designation: 'Graphic Designer', salary: 41000, joiningDate: '2026-04-20'},
  {id: 1016, name: 'Nikhil Bansal', email: 'nikhil.b@example.com', phone: '9876543225', department: 'Operations', designation: 'Operations Executive', salary: 39000, joiningDate: '2026-07-06'},
  {id: 1017, name: 'Pooja Reddy', email: 'pooja.r@example.com', phone: '9876543226', department: 'Engineering', designation: 'Java Developer', salary: 63000, joiningDate: `${currentMonth}-01`},
  {id: 1018, name: 'Aman Gupta', email: 'aman.g@example.com', phone: '9876543227', department: 'Marketing', designation: 'SEO Specialist', salary: 43000, joiningDate: '2026-08-10'},
  {id: 1019, name: 'Sakshi Jain', email: 'sakshi.j@example.com', phone: '9876543228', department: 'Finance', designation: 'Finance Executive', salary: 47000, joiningDate: '2026-03-23'},
  {id: 1020, name: 'Karan Sethi', email: 'karan.s@example.com', phone: '9876543229', department: 'Engineering', designation: 'Technical Lead', salary: 95000, joiningDate: '2024-09-02'},
  {id: 1021, name: 'Tanvi More', email: 'tanvi.m@example.com', phone: '9876543230', department: 'Human Resources', designation: 'HR Executive', salary: 38000, joiningDate: `${currentMonth}-01`},
  {id: 1022, name: 'Varun Menon', email: 'varun.m@example.com', phone: '9876543231', department: 'Operations', designation: 'Office Administrator', salary: 36000, joiningDate: '2025-10-13'},
  {id: 1023, name: 'Riya Choudhary', email: 'riya.c@example.com', phone: '9876543232', department: 'Design', designation: 'Visual Designer', salary: 52000, joiningDate: '2026-09-14'},
  {id: 1024, name: 'Abhishek Das', email: 'abhishek.d@example.com', phone: '9876543233', department: 'Engineering', designation: 'Database Developer', salary: 67000, joiningDate: '2026-02-02'}
];
let employees;
let storageAvailable = true;
try {
  const saved = apiMode ? null : localStorage.getItem(STORAGE_KEY);
  employees = saved ? JSON.parse(saved) : structuredClone(sampleEmployees);
  if (!Array.isArray(employees) || employees.some(e => !Number.isSafeInteger(e.id) || typeof e.name !== 'string' || typeof e.email !== 'string' || typeof e.department !== 'string' || typeof e.designation !== 'string' || !Number.isFinite(e.salary) || typeof e.joiningDate !== 'string')) throw new Error('Invalid saved data');
  // Expand existing browser demos once, without replacing user records or re-adding deleted samples.
  if (!apiMode && !localStorage.getItem(STORAGE_KEY + '.expanded24')) {
    if (saved) {
      const usedIds = new Set(employees.map(e => e.id));
      const usedEmails = new Set(employees.map(e => e.email.toLowerCase()));
      for (const sample of sampleEmployees.slice(8)) {
        if (usedEmails.has(sample.email.toLowerCase())) continue;
        const added = {...sample};
        if (usedIds.has(added.id)) added.id = Math.max(1000, ...usedIds) + 1;
        usedIds.add(added.id); usedEmails.add(added.email.toLowerCase()); employees.push(added);
      }
    }
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(employees));
      localStorage.setItem(STORAGE_KEY + '.expanded24', 'true');
    } catch { storageAvailable = false; }
  }
} catch {
  employees = structuredClone(sampleEmployees);
  storageAvailable = false;
}
if (apiMode) employees = [];
let page = 'dashboard';
let deleteId = null;
let toastTimer;
const money = new Intl.NumberFormat('en-IN', {style: 'currency', currency: 'INR', maximumFractionDigits: 0});
const dateFormat = new Intl.DateTimeFormat('en-IN', {day: '2-digit', month: 'short', year: 'numeric'});
function escapeHtml(value) { return String(value).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
function initials(name) { return name.split(/\s+/).filter(Boolean).slice(0,2).map(n => n[0]).join('').toUpperCase(); }
function toast(message) { clearTimeout(toastTimer); $('#toast').textContent = message; $('#toast').hidden = false; toastTimer = setTimeout(() => { $('#toast').hidden = true; }, 4000); }
async function api(path, method = 'GET', data) {
  let response;
  try {
    response = await fetch('/api' + path, {method, credentials:'same-origin', headers:{'Content-Type':'application/json','X-CSRF-Token':csrfToken}, body:data === undefined ? undefined : JSON.stringify(data)});
  } catch { throw new Error('Cannot reach the Java server. Check that it is running.'); }
  let result;
  try { result = await response.json(); } catch { throw new Error('Java API unavailable. Open the site using the Java server URL.'); }
  if (!response.ok) {
    if (response.status === 401 && path !== '/login') showLogin();
    throw new Error(result.error || 'Request failed. Please try again.');
  }
  return result;
}
function showLogin() {
  $('#app-view').hidden = true; $('#login-view').hidden = false;
  document.querySelectorAll('dialog[open]').forEach(dialog => dialog.close());
  backendReady = false; csrfToken = ''; employees = []; render();
}
async function loadEmployees() { employees = await api('/employees'); backendReady = true; render(); }
function persist(nextEmployees) {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(nextEmployees)); storageAvailable = true; }
  catch { storageAvailable = false; }
  employees = nextEmployees;
}
function render() {
  const departments = [...new Set(employees.map(e => e.department))].sort();
  const selected = $('#department-filter').value;
  $('#department-filter').innerHTML = '<option value="">All departments</option>' + departments.map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
  $('#department-filter').value = departments.includes(selected) ? selected : '';
  const search = $('#search').value.trim().toLowerCase();
  const filter = $('#department-filter').value;
  const filtered = employees.filter(e => (!filter || e.department === filter) && `${e.name} ${e.id} EMP-${e.id} ${e.email}`.toLowerCase().includes(search));
  $('#total-stat').textContent = employees.length;
  $('#nav-count').textContent = employees.length;
  $('#table-count').textContent = employees.length;
  $('#department-stat').textContent = departments.length;
  $('#salary-stat').textContent = money.format(employees.reduce((sum,e) => sum + e.salary, 0));
  $('#new-stat').textContent = employees.filter(e => e.joiningDate.startsWith(currentMonth)).length;
  $('#month-caption').textContent = `Joined in ${new Intl.DateTimeFormat('en-IN', {month:'long',year:'numeric'}).format(new Date())}`;
  const distribution = departments.map(d => ({name:d, count:employees.filter(e => e.department === d).length})).sort((a,b) => b.count-a.count);
  $('#department-bars').innerHTML = distribution.length ? distribution.slice(0,4).map(d => `<div class="department-row"><span>${escapeHtml(d.name)}</span><div class="bar-track"><div class="bar-fill" style="width:${d.count / employees.length * 100}%"></div></div><b>${d.count}</b></div>`).join('') : '<p class="muted">Add employees to see your team distribution.</p>';
  if (distribution.length > 4) $('#department-bars').insertAdjacentHTML('beforeend', `<p class="muted" style="font-size:9px">+ ${distribution.length-4} more department${distribution.length > 5 ? 's' : ''}</p>`);
  $('#employee-rows').innerHTML = filtered.map(e => {
    const tone = departments.indexOf(e.department) % 5;
    return `<tr><td><div class="employee-cell"><span class="avatar tone-${tone}">${escapeHtml(initials(e.name))}</span><div><strong>${escapeHtml(e.name)}</strong><small>${escapeHtml(e.email)}</small></div></div></td><td>EMP-${e.id}</td><td><span class="department-pill tone-${tone}">${escapeHtml(e.department)}</span></td><td>${escapeHtml(e.designation)}</td><td>${escapeHtml(dateFormat.format(new Date(e.joiningDate + 'T00:00:00')))}</td><td><div class="row-actions"><button data-edit="${e.id}" aria-label="Edit ${escapeHtml(e.name)}" title="Edit employee">✎</button><button data-delete="${e.id}" class="delete-action" aria-label="Delete ${escapeHtml(e.name)}" title="Delete employee">×</button></div></td></tr>`;
  }).join('');
  $('#empty-state').hidden = filtered.length > 0;
  $('#result-count').textContent = `${filtered.length} result${filtered.length === 1 ? '' : 's'}`;
  $('#table-summary').textContent = `Showing ${filtered.length} of ${employees.length} employees`;
  $('.local-note').textContent = apiMode ? (backendReady ? '● Connected to MySQL' : '● Database not loaded') : (storageAvailable ? '● Saved in this browser' : '● Temporary data · browser storage unavailable');
}
function navigate(next) {
  page = next === 'employees' ? 'employees' : 'dashboard';
  $('#overview-content').hidden = page !== 'dashboard';
  $('#breadcrumb').textContent = page === 'dashboard' ? 'Overview' : 'Employees';
  $('#page-title').innerHTML = page === 'dashboard' ? 'Team overview<span>.</span>' : 'Your people<span>.</span>';
  $('#page-description').textContent = page === 'dashboard' ? 'A little clarity for everything that makes your team.' : 'One place for every name, detail, and new beginning.';
  document.querySelectorAll('[data-page]').forEach(button => { button.classList.toggle('active', button.dataset.page === page); if(button.dataset.page === page) button.setAttribute('aria-current','page'); else button.removeAttribute('aria-current'); });
  history.replaceState(null,'',`#${page}`);
}
function openEmployee(id) {
  const form = $('#employee-form');
  form.reset();
  form.elements.id.value = '';
  $('#form-error').textContent = '';
  const employee = employees.find(e => e.id === id);
  $('#form-title').textContent = employee ? 'Edit employee' : 'Add employee';
  $('#save-button').textContent = employee ? 'Save changes' : 'Save employee';
  if (employee) Object.entries(employee).forEach(([key,value]) => { if(form.elements.namedItem(key)) form.elements.namedItem(key).value = value; });
  else form.elements.joiningDate.value = new Date().toLocaleDateString('en-CA');
  $('#employee-dialog').showModal();
}
$('#add-employee').addEventListener('click', () => openEmployee());
$('#view-team').addEventListener('click', () => navigate('employees'));
document.querySelectorAll('[data-page]').forEach(button => button.addEventListener('click', () => navigate(button.dataset.page)));
window.addEventListener('hashchange', () => navigate(location.hash.slice(1)));
document.querySelectorAll('[data-close]').forEach(button => button.addEventListener('click', () => document.getElementById(button.dataset.close).close()));
$('#search').addEventListener('input', render);
$('#department-filter').addEventListener('change', render);
$('#clear-filters').addEventListener('click', () => { $('#search').value = ''; $('#department-filter').value = ''; render(); });
$('#employee-rows').addEventListener('click', event => {
  const edit = event.target.closest('[data-edit]');
  const remove = event.target.closest('[data-delete]');
  if (edit) openEmployee(Number(edit.dataset.edit));
  if (remove) {
    deleteId = Number(remove.dataset.delete);
    const employee = employees.find(e => e.id === deleteId);
    $('#delete-description').textContent = `${employee.name} (EMP-${employee.id}) will be removed from ${apiMode ? 'the database' : 'this browser’s employee records'}. This cannot be undone.`;
    $('#delete-dialog').showModal();
  }
});
$('#employee-form').addEventListener('submit', async event => {
  event.preventDefault();
  const data = new FormData(event.currentTarget);
  const id = Number(data.get('id')) || Math.max(1000, ...employees.map(e => e.id)) + 1;
  const employee = {id, name:String(data.get('name')).trim(), email:String(data.get('email')).trim().toLowerCase(), phone:String(data.get('phone')).trim(), department:String(data.get('department')).trim(), designation:String(data.get('designation')).trim(), salary:Number(data.get('salary')), joiningDate:String(data.get('joiningDate'))};
  if (!employee.name || !employee.department || !employee.designation || !Number.isFinite(employee.salary) || employee.salary < 0 || !Number.isFinite(new Date(employee.joiningDate).getTime())) { $('#form-error').textContent = 'Please enter valid details in every field.'; return; }
  if (employees.some(e => e.id !== id && e.email.toLowerCase() === employee.email)) { $('#form-error').textContent = 'An employee with this email address already exists.'; return; }
  const editing = employees.some(e => e.id === id);
  if (apiMode) {
    const button = $('#save-button'); button.disabled = true;
    try {
      const saved = await api(editing ? `/employees/${id}` : '/employees', editing ? 'PUT' : 'POST', employee);
      employees = editing ? employees.map(e => e.id === id ? saved : e) : [...employees,saved];
      $('#search').value = ''; $('#department-filter').value = ''; render(); $('#employee-dialog').close(); toast(editing ? 'Employee updated in MySQL.' : 'Employee saved to MySQL.');
    } catch (error) { $('#form-error').textContent = error.message; }
    finally { button.disabled = false; }
    return;
  }
  persist(editing ? employees.map(e => e.id === id ? employee : e) : [...employees, employee]);
  $('#search').value = ''; $('#department-filter').value = '';
  render(); $('#employee-dialog').close();
  toast(storageAvailable ? (editing ? 'Employee details updated.' : 'New employee added to your team.') : 'Updated for this session. Browser storage is unavailable.');
});
$('#delete-form').addEventListener('submit', async event => {
  event.preventDefault();
  const button = $('#delete-form button[type="submit"]'); button.disabled = true;
  try {
    if (apiMode) { await api(`/employees/${deleteId}`,'DELETE'); employees = employees.filter(e => e.id !== deleteId); }
    else persist(employees.filter(e => e.id !== deleteId));
    render(); $('#delete-dialog').close(); toast(apiMode ? 'Employee deleted from MySQL.' : (storageAvailable ? 'Employee deleted.' : 'Deleted for this session. Browser storage is unavailable.')); deleteId = null;
  } catch (error) { toast(error.message); }
  finally { button.disabled = false; }
});
$('#export-csv').addEventListener('click', () => {
  // Prefix spreadsheet formula markers so exported text is treated as data.
  const cell = value => '"' + String(value).replace(/^[=+@\-\t\r]/, "'$&").replace(/"/g,'""') + '"';
  const rows = [['Employee ID','Name','Email','Phone','Department','Designation','Monthly Salary INR','Joining Date'], ...employees.filter(e => (!$('#department-filter').value || e.department === $('#department-filter').value) && `${e.name} ${e.id} EMP-${e.id} ${e.email}`.toLowerCase().includes($('#search').value.trim().toLowerCase())).map(e => [`EMP-${e.id}`,e.name,e.email,e.phone,e.department,e.designation,e.salary,e.joiningDate])];
  const url = URL.createObjectURL(new Blob(['\uFEFF' + rows.map(row => row.map(cell).join(',')).join('\r\n')], {type:'text/csv;charset=utf-8;'}));
  const link = document.createElement('a'); link.href = url; link.download = 'peopledesk-employees.csv'; document.body.append(link); link.click(); link.remove(); setTimeout(() => URL.revokeObjectURL(url),1000);
  toast('Employee export downloaded.');
});
$('#logout').addEventListener('click', async () => {
  if (apiMode) { try { await api('/logout','POST'); } catch (error) { toast(error.message); return; } showLogin(); }
  else { $('#app-view').hidden = true; $('#login-view').hidden = false; }
  $('#login-form').reset(); $('#login-error').textContent = ''; $('#username').focus();
});
$('#login-form').addEventListener('submit', async event => {
  event.preventDefault();
  if (apiMode) {
    const button = $('#login-form button[type="submit"]'); button.disabled = true; $('#login-error').textContent = '';
    try {
      const session = await api('/login','POST',{username:$('#username').value.trim(),password:$('#password').value}); csrfToken = session.csrfToken;
      await loadEmployees(); $('#login-view').hidden = true; $('#app-view').hidden = false; $('#password').value = ''; navigate('dashboard'); toast('Signed in. Your database is connected.');
    } catch (error) { $('#login-error').textContent = error.message; }
    finally { button.disabled = false; }
    return;
  }
  if ($('#username').value.trim() === 'admin' && $('#password').value === 'demo123') { $('#login-view').hidden = true; $('#app-view').hidden = false; navigate('dashboard'); toast('Welcome to your people workspace.'); }
  else $('#login-error').textContent = 'For this preview, use admin and demo123.';
});
// Prepare dashboard content while keeping the login page visible until sign-in.
render(); navigate(location.hash.slice(1));
if (apiMode) {
  showLogin();
  $('.preview-badge').innerHTML = '<i></i> Java + MySQL';
  $('.demo-note').innerHTML = '<strong>Administrator sign-in</strong><p>Use the administrator account configured when the Java backend was first started. Demo credentials do not work here.</p>';
  $('#login-error').textContent = 'Checking connection…';
  api('/session').then(async session => {
    csrfToken = session.csrfToken; await loadEmployees(); $('#login-view').hidden = true; $('#app-view').hidden = false; $('#login-error').textContent = '';
  }).catch(error => { $('#login-error').textContent = error.message === 'Please sign in to continue.' ? '' : error.message; });
}
