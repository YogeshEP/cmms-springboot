// ===== Guard: must be logged in =====
if (!CmmsApi.getToken()) {
  window.location.href = '/index.html';
}

document.getElementById('userTag').textContent = `${CmmsApi.getUsername()} · ${CmmsApi.getRole()}`;
document.getElementById('roleBadge').textContent = CmmsApi.getRole();
document.getElementById('logoutBtn').addEventListener('click', () => CmmsApi.logout());

const isAdmin = () => CmmsApi.getRole() === 'ROLE_ADMIN' || CmmsApi.getRole() === 'ADMIN';

// ===== Toast =====
function toast(msg, isError = false) {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.className = 'toast show' + (isError ? ' error' : '');
  setTimeout(() => { el.className = 'toast'; }, 3000);
}

// ===== Beacon helpers =====
function beacon(text, color) {
  return `<span class="beacon beacon-${color}"><span class="bulb"></span>${text}</span>`;
}
const ASSET_STATUS_COLOR = { OPERATIONAL: 'green', UNDER_MAINTENANCE: 'amber', BREAKDOWN: 'red', RETIRED: 'gray' };
const WO_STATUS_COLOR = { OPEN: 'blue', IN_PROGRESS: 'amber', ON_HOLD: 'gray', COMPLETED: 'green', CANCELLED: 'red' };
const WO_PRIORITY_COLOR = { LOW: 'gray', MEDIUM: 'blue', HIGH: 'amber', CRITICAL: 'red' };

// ===== Reference data cache (for dropdowns) =====
let refCache = { locations: [], technicians: [], assets: [] };

async function loadRefCache() {
  try {
    const [locations, technicians, assets] = await Promise.all([
      CmmsApi.get('/api/locations'),
      CmmsApi.get('/api/technicians'),
      CmmsApi.get('/api/assets')
    ]);
    refCache = { locations: locations || [], technicians: technicians || [], assets: assets || [] };
  } catch (e) {
    console.error('Failed to load reference data', e);
  }
}

// ===== View configs =====
const VIEWS = {
  assets: {
    title: 'Assets',
    path: '/api/assets',
    columns: [
      { key: 'assetCode', label: 'Code' },
      { key: 'name', label: 'Name' },
      { key: 'category', label: 'Category' },
      { key: 'status', label: 'Status', render: v => beacon(v, ASSET_STATUS_COLOR[v] || 'gray') },
      { key: 'location', label: 'Location', render: v => v?.name || '—' },
      { key: 'manufacturer', label: 'Manufacturer' }
    ],
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true },
      { key: 'assetCode', label: 'Asset Code', type: 'text', required: true },
      { key: 'category', label: 'Category', type: 'text' },
      { key: 'manufacturer', label: 'Manufacturer', type: 'text' },
      { key: 'model', label: 'Model', type: 'text' },
      { key: 'serialNumber', label: 'Serial Number', type: 'text' },
      { key: 'purchaseDate', label: 'Purchase Date', type: 'date' },
      { key: 'status', label: 'Status', type: 'select', options: Object.keys(ASSET_STATUS_COLOR), default: 'OPERATIONAL' },
      { key: 'location', label: 'Location', type: 'ref-select', refList: () => refCache.locations }
    ]
  },
  workorders: {
    title: 'Work Orders',
    path: '/api/work-orders',
    columns: [
      { key: 'title', label: 'Title' },
      { key: 'asset', label: 'Asset', render: v => v?.name || '—' },
      { key: 'technician', label: 'Technician', render: v => v?.name || '—' },
      { key: 'priority', label: 'Priority', render: v => beacon(v, WO_PRIORITY_COLOR[v] || 'gray') },
      { key: 'status', label: 'Status', render: v => beacon(v, WO_STATUS_COLOR[v] || 'gray') },
      { key: 'dueDate', label: 'Due' }
    ],
    fields: [
      { key: 'title', label: 'Title', type: 'text', required: true },
      { key: 'description', label: 'Description', type: 'textarea' },
      { key: 'asset', label: 'Asset', type: 'ref-select', refList: () => refCache.assets, required: true },
      { key: 'technician', label: 'Technician', type: 'ref-select', refList: () => refCache.technicians },
      { key: 'type', label: 'Type', type: 'select', options: ['PREVENTIVE', 'CORRECTIVE', 'INSPECTION', 'EMERGENCY'], default: 'CORRECTIVE' },
      { key: 'priority', label: 'Priority', type: 'select', options: Object.keys(WO_PRIORITY_COLOR), default: 'MEDIUM' },
      { key: 'dueDate', label: 'Due Date', type: 'date' }
    ],
    extraRowActions: (row) => [
      { label: 'Complete', onClick: async () => {
          await CmmsApi.patch(`/api/work-orders/${row.id}/status`, { status: 'COMPLETED' });
          toast('Work order marked completed');
          renderView('workorders');
        }
      }
    ]
  },
  schedules: {
    title: 'Maintenance Schedules',
    path: '/api/maintenance-schedules',
    columns: [
      { key: 'asset', label: 'Asset', render: v => v?.name || '—' },
      { key: 'frequency', label: 'Frequency' },
      { key: 'nextMaintenanceDate', label: 'Next Due' },
      { key: 'active', label: 'Active', render: v => v ? beacon('ACTIVE', 'green') : beacon('INACTIVE', 'gray') }
    ],
    fields: [
      { key: 'asset', label: 'Asset', type: 'ref-select', refList: () => refCache.assets, required: true },
      { key: 'frequency', label: 'Frequency', type: 'select', options: ['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY'], default: 'MONTHLY' },
      { key: 'description', label: 'Description', type: 'textarea' },
      { key: 'lastMaintenanceDate', label: 'Last Maintenance', type: 'date' },
      { key: 'nextMaintenanceDate', label: 'Next Maintenance', type: 'date' }
    ]
  },
  technicians: {
    title: 'Technicians',
    path: '/api/technicians',
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'email', label: 'Email' },
      { key: 'phone', label: 'Phone' },
      { key: 'specialization', label: 'Specialization' }
    ],
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true },
      { key: 'email', label: 'Email', type: 'text' },
      { key: 'phone', label: 'Phone', type: 'text' },
      { key: 'specialization', label: 'Specialization', type: 'text' }
    ]
  },
  locations: {
    title: 'Locations',
    path: '/api/locations',
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'description', label: 'Description' }
    ],
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true },
      { key: 'description', label: 'Description', type: 'textarea' }
    ]
  },
  parts: {
    title: 'Spare Parts',
    path: '/api/spare-parts',
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'partNumber', label: 'Part #' },
      { key: 'quantityInStock', label: 'In Stock',
        render: (v, row) => v <= row.reorderLevel ? beacon(v, 'red') : v },
      { key: 'reorderLevel', label: 'Reorder At' },
      { key: 'unitCost', label: 'Unit Cost', render: v => v != null ? `$${Number(v).toFixed(2)}` : '—' }
    ],
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true },
      { key: 'partNumber', label: 'Part Number', type: 'text', required: true },
      { key: 'quantityInStock', label: 'Quantity In Stock', type: 'number', default: 0 },
      { key: 'reorderLevel', label: 'Reorder Level', type: 'number', default: 5 },
      { key: 'unitCost', label: 'Unit Cost', type: 'number', step: '0.01' }
    ]
  }
};

let currentView = 'overview';

// ===== Nav wiring =====
document.querySelectorAll('.nav-item').forEach(item => {
  item.addEventListener('click', () => {
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
    item.classList.add('active');
    const view = item.dataset.view;
    currentView = view;
    document.getElementById('viewTitle').textContent = VIEWS[view]?.title || 'Overview';
    if (view === 'overview') renderOverview(); else renderView(view);
  });
});

// ===== Overview =====
async function renderOverview() {
  const content = document.getElementById('content');
  content.innerHTML = `<div class="stat-grid" id="statGrid"></div>
    <div class="panel-head"><h2>Recent Work Orders</h2></div>
    <div class="table-wrap"><table><thead><tr>
      <th>Title</th><th>Asset</th><th>Priority</th><th>Status</th><th>Due</th>
    </tr></thead><tbody id="recentWo"><tr class="loading-row"><td colspan="5">Loading…</td></tr></tbody></table></div>`;

  try {
    await loadRefCache();
    const [workOrders, parts] = await Promise.all([
      CmmsApi.get('/api/work-orders'),
      CmmsApi.get('/api/spare-parts/low-stock')
    ]);

    const openCount = (workOrders || []).filter(w => w.status === 'OPEN' || w.status === 'IN_PROGRESS').length;

    document.getElementById('statGrid').innerHTML = `
      ${statCard('Assets', refCache.assets.length)}
      ${statCard('Open Work Orders', openCount)}
      ${statCard('Technicians', refCache.technicians.length)}
      ${statCard('Low Stock Parts', (parts || []).length)}
    `;

    const rows = (workOrders || []).slice(0, 8);
    const tbody = document.getElementById('recentWo');
    tbody.innerHTML = rows.length ? rows.map(r => `<tr>
        <td>${escapeHtml(r.title)}</td>
        <td>${escapeHtml(r.asset?.name || '—')}</td>
        <td>${beacon(r.priority, WO_PRIORITY_COLOR[r.priority] || 'gray')}</td>
        <td>${beacon(r.status, WO_STATUS_COLOR[r.status] || 'gray')}</td>
        <td>${r.dueDate || '—'}</td>
      </tr>`).join('') : `<tr class="empty-row"><td colspan="5">No work orders yet</td></tr>`;
  } catch (e) {
    toast(e.message, true);
  }
}

function statCard(label, value) {
  return `<div class="stat-card"><div class="label">${label}</div><div class="value">${value}</div></div>`;
}

function escapeHtml(str) {
  if (str == null) return '';
  return String(str).replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
}

// ===== Generic table view =====
async function renderView(viewKey) {
  const cfg = VIEWS[viewKey];
  const content = document.getElementById('content');

  content.innerHTML = `
    <div class="panel-head">
      <h2>${cfg.title} <span class="count" id="rowCount"></span></h2>
      <button class="btn btn-amber" id="addBtn">+ New</button>
    </div>
    <div class="table-wrap">
      <table>
        <thead><tr>
          ${cfg.columns.map(c => `<th>${c.label}</th>`).join('')}
          <th>Actions</th>
        </tr></thead>
        <tbody id="tableBody"><tr class="loading-row"><td colspan="${cfg.columns.length + 1}">Loading…</td></tr></tbody>
      </table>
    </div>
  `;

  document.getElementById('addBtn').addEventListener('click', () => openDrawer(viewKey, null));

  try {
    await loadRefCache();
    const data = await CmmsApi.get(cfg.path);
    document.getElementById('rowCount').textContent = `(${(data || []).length})`;
    renderRows(viewKey, data || []);
  } catch (e) {
    toast(e.message, true);
    document.getElementById('tableBody').innerHTML = `<tr class="empty-row"><td colspan="${cfg.columns.length + 1}">Failed to load</td></tr>`;
  }
}

function renderRows(viewKey, data) {
  const cfg = VIEWS[viewKey];
  const tbody = document.getElementById('tableBody');

  if (!data.length) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="${cfg.columns.length + 1}">No records yet — click "+ New" to add one</td></tr>`;
    return;
  }

  tbody.innerHTML = data.map(row => {
    const cells = cfg.columns.map(c => {
      const raw = row[c.key];
      const html = c.render ? c.render(raw, row) : escapeHtml(raw ?? '—');
      return `<td>${html}</td>`;
    }).join('');

    const extra = (cfg.extraRowActions ? cfg.extraRowActions(row) : [])
      .map((a, idx) => `<button class="icon-btn" data-extra="${idx}">${a.label}</button>`).join('');

    return `<tr data-id="${row.id}">
      ${cells}
      <td class="actions-cell">
        <button class="icon-btn" data-edit>Edit</button>
        ${extra}
        ${isAdmin() ? '<button class="icon-btn danger" data-delete>Delete</button>' : ''}
      </td>
    </tr>`;
  }).join('');

  // wire row actions
  tbody.querySelectorAll('tr').forEach(tr => {
    const id = tr.dataset.id;
    const row = data.find(r => String(r.id) === String(id));

    tr.querySelector('[data-edit]')?.addEventListener('click', () => openDrawer(viewKey, row));

    tr.querySelector('[data-delete]')?.addEventListener('click', async () => {
      if (!confirm(`Delete this ${VIEWS[viewKey].title.slice(0, -1) || 'record'}? This can't be undone.`)) return;
      try {
        await CmmsApi.del(`${cfg.path}/${id}`);
        toast('Deleted');
        renderView(viewKey);
      } catch (e) {
        toast(e.message, true);
      }
    });

    const extraActions = cfg.extraRowActions ? cfg.extraRowActions(row) : [];
    extraActions.forEach((a, idx) => {
      tr.querySelector(`[data-extra="${idx}"]`)?.addEventListener('click', async () => {
        try { await a.onClick(); } catch (e) { toast(e.message, true); }
      });
    });
  });
}

// ===== Drawer (create/edit form) =====
const overlay = document.getElementById('overlay');
const drawerBody = document.getElementById('drawerBody');
const drawerTitle = document.getElementById('drawerTitle');

document.getElementById('drawerClose').addEventListener('click', closeDrawer);
document.getElementById('drawerCancel').addEventListener('click', closeDrawer);
overlay.addEventListener('click', (e) => { if (e.target === overlay) closeDrawer(); });

let activeViewKey = null;
let activeRecord = null;

function openDrawer(viewKey, record) {
  activeViewKey = viewKey;
  activeRecord = record;
  const cfg = VIEWS[viewKey];
  drawerTitle.textContent = record ? `Edit ${cfg.title.slice(0, -1)}` : `New ${cfg.title.slice(0, -1)}`;

  drawerBody.innerHTML = cfg.fields.map(f => renderField(f, record)).join('');
  overlay.classList.add('open');
}

function closeDrawer() {
  overlay.classList.remove('open');
  activeViewKey = null;
  activeRecord = null;
}

function renderField(f, record) {
  const current = record ? record[f.key] : undefined;

  if (f.type === 'select') {
    const val = current ?? f.default ?? '';
    const opts = f.options.map(o => `<option value="${o}" ${o === val ? 'selected' : ''}>${o}</option>`).join('');
    return `<div class="field"><label>${f.label}</label><select data-field="${f.key}">${opts}</select></div>`;
  }

  if (f.type === 'ref-select') {
    const list = f.refList();
    const currentId = current?.id ?? '';
    const opts = ['<option value="">— none —</option>']
      .concat(list.map(item => `<option value="${item.id}" ${String(item.id) === String(currentId) ? 'selected' : ''}>${escapeHtml(item.name)}</option>`))
      .join('');
    return `<div class="field"><label>${f.label}${f.required ? ' *' : ''}</label><select data-field="${f.key}" data-ref="true">${opts}</select></div>`;
  }

  if (f.type === 'textarea') {
    return `<div class="field"><label>${f.label}</label><textarea data-field="${f.key}">${escapeHtml(current ?? '')}</textarea></div>`;
  }

  const val = current ?? f.default ?? '';
  return `<div class="field"><label>${f.label}${f.required ? ' *' : ''}</label>
    <input type="${f.type}" data-field="${f.key}" value="${escapeHtml(val)}" ${f.step ? `step="${f.step}"` : ''}></div>`;
}

document.getElementById('drawerSave').addEventListener('click', async () => {
  if (!activeViewKey) return;
  const cfg = VIEWS[activeViewKey];
  const payload = {};

  cfg.fields.forEach(f => {
    const el = drawerBody.querySelector(`[data-field="${f.key}"]`);
    if (!el) return;

    if (f.type === 'ref-select') {
      payload[f.key] = el.value ? { id: Number(el.value) } : null;
    } else if (f.type === 'number') {
      payload[f.key] = el.value === '' ? null : Number(el.value);
    } else {
      payload[f.key] = el.value === '' ? null : el.value;
    }
  });

  // basic required-field check
  const missing = cfg.fields.filter(f => f.required && !payload[f.key]);
  if (missing.length) {
    toast(`Please fill in: ${missing.map(f => f.label).join(', ')}`, true);
    return;
  }

  try {
    if (activeRecord) {
      await CmmsApi.put(`${cfg.path}/${activeRecord.id}`, payload);
      toast('Updated successfully');
    } else {
      await CmmsApi.post(cfg.path, payload);
      toast('Created successfully');
    }
    closeDrawer();
    renderView(activeViewKey);
  } catch (e) {
    toast(e.message, true);
  }
});

// ===== Init =====
renderOverview();
