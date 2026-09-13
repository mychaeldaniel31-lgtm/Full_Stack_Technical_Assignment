const {test} = require('node:test');
const assert = require('node:assert/strict');
const {readFileSync} = require('node:fs');
const vm = require('node:vm');

function app() {
  const elements = {};
  const requests = [];
  const context = vm.createContext({
    document: {getElementById: id => elements[id] ??= {value: '', reset() {}}},
    localStorage: {getItem() {return null}, setItem() {}, removeItem() {}},
    location: {reload() {}}, Date, console,
    fetch: async (url, options) => {
      requests.push({url, options});
      return {ok: true, json: async () => url.includes('appointments') && !options.method
        ? [{title: '<img src=x onerror=alert(1)>', creator: '<b>user</b>', invitees: ['<script>'],
            start: '2040-01-01T01:00:00Z', end: '2040-01-01T02:00:00Z'}] : {}};
    }
  });
  vm.runInContext(readFileSync('src/main/resources/static/app.js', 'utf8'), context);
  vm.runInContext("me={name:'Siti',preferredTimezone:'Asia/Jakarta'}", context);
  return {context, elements, requests};
}
test('UTC form values remain UTC regardless of browser timezone', async () => {
  process.env.TZ = 'Asia/Jakarta';
  const {elements, requests} = app();
  for (const [id,value] of Object.entries({title:'Meeting', start:'2040-01-01T08:00',end:'2040-01-01T09:00',invitees:''})) {
    elements[id] ??= {}; elements[id].value = value;
  }
  elements.formMessage = {};
  await elements.appointmentForm.onsubmit({preventDefault() {}, target:{reset() {}}});
  assert.equal(JSON.parse(requests[0].options.body).start, '2040-01-01T08:00:00.000Z');
});
test('appointment content cannot inject HTML', async () => {
  const {context, elements} = app();
  await vm.runInContext('load()', context);
  assert.ok(!elements.list.innerHTML.includes('<img'));
  assert.ok(!elements.list.innerHTML.includes('<b>'));
  assert.ok(!elements.list.innerHTML.includes('<script>'));
});
test('logout requests server-side token revocation', async () => {
  const {elements, requests} = app();
  await elements.logout.onclick();
  assert.ok(requests.some(r => r.url === '/api/auth/logout' && r.options.method === 'POST'));
});
test('expired session returns the UI to login', async () => {
  const {context, elements} = app();
  vm.runInContext("fetch=async()=>({ok:false,status:401,json:async()=>({message:'Sesi kedaluwarsa'})})", context);
  await assert.rejects(vm.runInContext("api('/me')", context));
  assert.equal(elements.login?.hidden, false);
  assert.equal(elements.app?.hidden, true);
});
test('appointment list can request subsequent pages', async () => {
  const {context, requests} = app();
  await vm.runInContext('page=1;load()', context);
  assert.ok(requests.some(r => r.url === '/api/appointments?page=1&size=50'));
});
