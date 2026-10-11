const $ = (id) => document.getElementById(id);
const serviceStatus = $('serviceStatus');
const stockValue = $('stockValue');
const toast = $('toast');
let toastTimer;

function notify(message, error = false) {
  toast.textContent = message;
  toast.className = `toast show${error ? ' error' : ''}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.className = 'toast'; }, 3200);
}

async function api(url, options = {}) {
  const response = await fetch(url, { headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }, ...options });
  const type = response.headers.get('content-type') || '';
  if (!type.includes('application/json')) {
    throw new Error('后端接口未启动：当前仅为静态页面预览');
  }
  const body = await response.json().catch(() => ({ success: false, message: '后端响应无法解析' }));
  if (!response.ok || !body.success) throw new Error(body.message || `请求失败 (${response.status})`);
  return body.data;
}

async function refreshVoucher(silent = false) {
  try {
    const raw = await api('/api/v1/vouchers/1');
    const voucher = raw ? JSON.parse(raw) : null;
    stockValue.textContent = voucher?.stock ?? '未初始化';
    serviceStatus.textContent = '服务已连接';
    if (!silent) notify('已获取 Redis / MySQL 商品详情');
  } catch (error) {
    stockValue.textContent = '--';
    serviceStatus.textContent = '服务未连接';
    if (!silent) notify(`无法获取详情：${error.message}`, true);
  }
}

function showResult(orderId) {
  $('emptyState').classList.add('hidden');
  $('resultState').classList.remove('hidden');
  $('orderId').textContent = `ORDER ${orderId}`;
  $('resultTag').textContent = 'PROCESSING';
  $('resultTag').classList.remove('persisted');
  $('resultTitle').textContent = '订单正在进入队列';
  $('resultMessage').textContent = 'Redis 已原子预扣库存，Kafka 消费者正在异步处理。';
  $('persistStep').classList.remove('done');
}

async function pollOrder(orderId, attempts = 0) {
  try {
    const order = await api(`/api/v1/orders/${orderId}`);
    if (order.status === 'PERSISTED') {
      $('resultTag').textContent = 'PERSISTED';
      $('resultTag').classList.add('persisted');
      $('resultTitle').textContent = '订单已落库';
      $('resultMessage').textContent = `用户 ${order.userId} 的订单已通过 MySQL 唯一索引幂等校验。`;
      $('persistStep').classList.add('done');
      refreshVoucher(true);
      return;
    }
    if (attempts < 10) setTimeout(() => pollOrder(orderId, attempts + 1), 700);
  } catch { if (attempts < 10) setTimeout(() => pollOrder(orderId, attempts + 1), 700); }
}

$('orderButton').addEventListener('click', async () => {
  const userId = Number($('userId').value);
  if (!Number.isInteger(userId) || userId < 1) return notify('请输入大于 0 的用户 ID', true);
  const button = $('orderButton'); button.disabled = true; button.firstChild.textContent = '正在提交... ';
  try {
    const result = await api('/api/v1/vouchers/1/orders', { method: 'POST', headers: { 'X-User-Id': String(userId) } });
    showResult(result.orderId); notify(`订单 ${result.orderId} 已被 Kafka 接收`); pollOrder(result.orderId);
  } catch (error) { notify(error.message, true); }
  finally { button.disabled = false; button.firstChild.textContent = '抢购 ¥100 秒杀券 '; refreshVoucher(true); }
});

$('refreshButton').addEventListener('click', () => refreshVoucher());
$('seedButton').addEventListener('click', async () => {
  const stock = Number($('seedStock').value);
  if (!Number.isInteger(stock) || stock < 0) return notify('库存必须是非负整数', true);
  try { await api(`/api/v1/vouchers/1/stock?stock=${stock}`, { method: 'POST' }); await refreshVoucher(true); notify(`本地 Redis 库存已初始化为 ${stock}`); }
  catch (error) { notify(error.message, true); }
});

refreshVoucher(true);
