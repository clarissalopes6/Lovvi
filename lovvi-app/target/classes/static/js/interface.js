const API_BASE = '/api/interface';

const state = {
  databaseName: '-',
  tables: [],
  filteredTables: [],
  selectedTableName: null,
  tableDetailsByName: new Map(),
  usuarios: [],
  testes: []
};

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function formatValue(value) {
  if (value === null || value === undefined) {
    return '<span class="db-null">null</span>';
  }

  if (typeof value === 'object') {
    return escapeHtml(JSON.stringify(value));
  }

  return escapeHtml(String(value));
}

function setText(id, text) {
  const el = document.getElementById(id);
  if (el) {
    el.textContent = text;
  }
}

function normalizeTableName(name) {
  return String(name || '').trim().toLowerCase();
}

function updateCrudVisibility() {
  const selectedTable = normalizeTableName(state.selectedTableName);
  const usuarioCard = document.getElementById('crudUsuarioCard');
  const testeCard = document.getElementById('crudTesteCard');
  const hint = document.getElementById('crudHint');

  const showUsuarioCrud = selectedTable === 'usuario';
  const showTesteCrud = selectedTable === 'teste';

  if (usuarioCard) {
    usuarioCard.hidden = !showUsuarioCrud;
  }

  if (testeCard) {
    testeCard.hidden = !showTesteCrud;
  }

  if (hint) {
    hint.hidden = showUsuarioCrud || showTesteCrud;
  }
}

function getTableSummaryByName(name) {
  return state.tables.find(t => t.tableName === name) || null;
}

function showCrudMessage(containerId, message, type) {
  const el = document.getElementById(containerId);
  if (!el) return;
  el.textContent = message;
  el.className = `crud-msg show ${type || 'info'}`;
}

function clearCrudMessage(containerId) {
  const el = document.getElementById(containerId);
  if (!el) return;
  el.textContent = '';
  el.className = 'crud-msg';
}

async function requestJson(url, options = {}) {
  const response = await fetch(url, options);
  const contentType = response.headers.get('content-type') || '';
  let payload = null;

  if (contentType.includes('application/json')) {
    payload = await response.json();
  } else {
    const text = await response.text();
    if (text) {
      try {
        payload = JSON.parse(text);
      } catch {
        payload = { message: text };
      }
    }
  }

  if (!response.ok) {
    throw new Error(payload?.message || `Erro ${response.status}`);
  }

  return payload;
}

function updateStats() {
  const table = getTableSummaryByName(state.selectedTableName);
  setText('selectedTable', table ? table.tableName : '-');
  setText('selectedColumns', table ? String(table.totalColumns) : '0');
  setText('selectedRows', table ? String(table.totalRows) : '0');
}

function renderTableList() {
  const list = document.getElementById('tableList');
  const count = document.getElementById('tablesCount');
  if (!list || !count) return;

  list.innerHTML = '';
  count.textContent = `${state.filteredTables.length} tabela(s)`;

  if (state.filteredTables.length === 0) {
    const empty = document.createElement('li');
    empty.className = 'db-list-empty';
    empty.textContent = 'Nenhuma tabela encontrada.';
    list.appendChild(empty);
    return;
  }

  state.filteredTables.forEach(table => {
    const item = document.createElement('li');
    item.className = 'db-table-item';

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = `db-table-btn ${state.selectedTableName === table.tableName ? 'active' : ''}`;
    btn.innerHTML = `
      <span class="db-table-name">${escapeHtml(table.tableName)}</span>
      <span class="db-table-meta">${table.totalColumns} colunas • ${table.totalRows} registros</span>
    `;

    btn.addEventListener('click', () => {
      state.selectedTableName = table.tableName;
      updateStats();
      updateCrudVisibility();
      renderTableList();
      loadTableDetails();
    });

    item.appendChild(btn);
    list.appendChild(item);
  });
}

function renderColumns(columns) {
  const table = document.getElementById('columnsGrid');
  if (!table) return;
  const tbody = table.querySelector('tbody');
  if (!tbody) return;

  if (!Array.isArray(columns) || columns.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4">Sem colunas para exibir.</td></tr>';
    return;
  }

  tbody.innerHTML = columns.map(col => `
    <tr>
      <td>${escapeHtml(col.name)}</td>
      <td>${escapeHtml(col.type)}</td>
      <td>${col.size}</td>
      <td>${col.nullable ? 'Sim' : 'Não'}</td>
    </tr>
  `).join('');
}

function renderRows(rows) {
  const wrap = document.getElementById('rowsGridWrap');
  if (!wrap) return;

  if (!Array.isArray(rows) || rows.length === 0) {
    wrap.innerHTML = '<p class="db-empty">Sem registros para exibir.</p>';
    return;
  }

  const columns = Object.keys(rows[0]);
  const header = columns.map(col => `<th>${col}</th>`).join('');
  const body = rows.map(row => {
    const cells = columns.map(col => `<td>${formatValue(row[col])}</td>`).join('');
    return `<tr>${cells}</tr>`;
  }).join('');

  wrap.innerHTML = `
    <table class="db-grid db-rows-grid">
      <thead><tr>${header}</tr></thead>
      <tbody>${body}</tbody>
    </table>
  `;
}

function applyFilter() {
  const search = document.getElementById('tableSearch');
  const term = (search?.value || '').trim().toLowerCase();

  state.filteredTables = term
    ? state.tables.filter(t => t.tableName.toLowerCase().includes(term))
    : [...state.tables];

  if (!state.filteredTables.some(t => t.tableName === state.selectedTableName)) {
    state.selectedTableName = state.filteredTables.length > 0 ? state.filteredTables[0].tableName : null;
  }

  updateStats();
  updateCrudVisibility();
  renderTableList();
  loadTableDetails().catch(error => {
    showGlobalError(error.message || 'Erro ao carregar detalhes da tabela.');
  });
}

async function refreshDatabasePanels(preferredTableName) {
  state.tableDetailsByName.clear();
  if (preferredTableName) {
    state.selectedTableName = preferredTableName;
  }
  await loadOverview();
}

async function loadOverview() {
  const data = await requestJson(`${API_BASE}/overview`);
  state.databaseName = data.databaseName || '-';
  state.tables = Array.isArray(data.tables) ? data.tables : [];
  state.filteredTables = [...state.tables];

  if (!state.selectedTableName && state.filteredTables.length > 0) {
    state.selectedTableName = state.filteredTables[0].tableName;
  }

  setText('dbName', state.databaseName);
  updateStats();
  updateCrudVisibility();
  renderTableList();
  await loadTableDetails();
}

async function loadTableDetails() {
  const tableName = state.selectedTableName;
  if (!tableName) {
    renderColumns([]);
    renderRows([]);
    return;
  }

  const limit = Number(document.getElementById('rowLimit')?.value || 50);
  const cacheKey = `${tableName}:${limit}`;

  if (state.tableDetailsByName.has(cacheKey)) {
    const cached = state.tableDetailsByName.get(cacheKey);
    renderColumns(cached.columns);
    renderRows(cached.rows);
    return;
  }

  const details = await requestJson(`${API_BASE}/tables/${encodeURIComponent(tableName)}?limit=${limit}`);
  state.tableDetailsByName.set(cacheKey, details);
  renderColumns(details.columns);
  renderRows(details.rows);
}

function fillUsuarioForm(usuario) {
  document.getElementById('usuarioId').value = usuario.idUsuario;
  document.getElementById('usuarioNome').value = usuario.nome || '';
  document.getElementById('usuarioSobrenome').value = usuario.sobrenome || '';
  document.getElementById('usuarioEmail').value = usuario.email || '';
  document.getElementById('usuarioSenha').value = '123456';
  document.getElementById('usuarioCidade').value = usuario.cidade || '';
  document.getElementById('usuarioGenero').value = usuario.genero || 'Outro';
  document.getElementById('usuarioNascimento').value = usuario.dtNascimento || '';
}

function resetUsuarioForm() {
  document.getElementById('usuarioId').value = '';
  document.getElementById('formUsuario')?.reset();
}

function renderUsuariosCrud() {
  const tbody = document.querySelector('#usuariosCrudGrid tbody');
  if (!tbody) return;

  if (!Array.isArray(state.usuarios) || state.usuarios.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6">Nenhum usuario encontrado.</td></tr>';
    return;
  }

  tbody.innerHTML = state.usuarios.map(usuario => `
    <tr>
      <td>${usuario.idUsuario}</td>
      <td>${escapeHtml(usuario.nome)} ${escapeHtml(usuario.sobrenome)}</td>
      <td>${escapeHtml(usuario.email)}</td>
      <td>${escapeHtml(usuario.cidade || '')}</td>
      <td>${escapeHtml(usuario.genero || '')}</td>
      <td class="crud-actions-cell">
        <button type="button" class="crud-mini-btn" data-action="edit-usuario" data-id="${usuario.idUsuario}">Editar</button>
        <button type="button" class="crud-mini-btn danger" data-action="delete-usuario" data-id="${usuario.idUsuario}">Excluir</button>
      </td>
    </tr>
  `).join('');
}

async function loadUsuariosCrud() {
  const data = await requestJson(`${API_BASE}/usuarios?limit=120`);
  state.usuarios = Array.isArray(data) ? data : [];
  renderUsuariosCrud();
}

async function handleUsuarioSubmit(event) {
  event.preventDefault();
  clearCrudMessage('usuariosCrudMsg');

  const id = document.getElementById('usuarioId').value;
  const payload = {
    nome: document.getElementById('usuarioNome').value,
    sobrenome: document.getElementById('usuarioSobrenome').value,
    email: document.getElementById('usuarioEmail').value,
    senha: document.getElementById('usuarioSenha').value,
    cidade: document.getElementById('usuarioCidade').value,
    genero: document.getElementById('usuarioGenero').value,
    dtNascimento: document.getElementById('usuarioNascimento').value
  };

  const isUpdate = Boolean(id);
  const url = isUpdate ? `${API_BASE}/usuarios/${id}` : `${API_BASE}/usuarios`;
  const method = isUpdate ? 'PUT' : 'POST';

  try {
    const result = await requestJson(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    showCrudMessage('usuariosCrudMsg', result?.message || 'Operacao realizada.', 'success');
    resetUsuarioForm();
    await loadUsuariosCrud();
    await refreshDatabasePanels('usuario');
  } catch (error) {
    showCrudMessage('usuariosCrudMsg', error.message || 'Falha ao salvar usuario.', 'error');
  }
}

async function handleUsuarioGridClick(event) {
  const button = event.target.closest('button[data-action]');
  if (!button) return;

  const id = Number(button.getAttribute('data-id'));
  const action = button.getAttribute('data-action');
  if (!id || !action) return;

  if (action === 'edit-usuario') {
    const usuario = state.usuarios.find(item => item.idUsuario === id);
    if (usuario) {
      fillUsuarioForm(usuario);
      showCrudMessage('usuariosCrudMsg', `Editando usuario ID ${id}.`, 'info');
    }
    return;
  }

  if (action === 'delete-usuario') {
    const confirmar = window.confirm(`Deseja remover o usuario ID ${id}?`);
    if (!confirmar) return;

    try {
      const result = await requestJson(`${API_BASE}/usuarios/${id}`, { method: 'DELETE' });
      showCrudMessage('usuariosCrudMsg', result?.message || 'Usuario removido.', 'success');
      await loadUsuariosCrud();
      await refreshDatabasePanels('usuario');
    } catch (error) {
      showCrudMessage('usuariosCrudMsg', error.message || 'Falha ao remover usuario.', 'error');
    }
  }
}

function fillTesteForm(teste) {
  document.getElementById('testeId').value = teste.idTeste;
  document.getElementById('testeNome').value = teste.nomeTeste || '';
  document.getElementById('testeDescricao').value = teste.descricao || '';
}

function resetTesteForm() {
  document.getElementById('testeId').value = '';
  document.getElementById('formTeste')?.reset();
}

function renderTestesCrud() {
  const tbody = document.querySelector('#testesCrudGrid tbody');
  if (!tbody) return;

  if (!Array.isArray(state.testes) || state.testes.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4">Nenhum teste encontrado.</td></tr>';
    return;
  }

  tbody.innerHTML = state.testes.map(teste => `
    <tr>
      <td>${teste.idTeste}</td>
      <td>${escapeHtml(teste.nomeTeste)}</td>
      <td>${escapeHtml(teste.descricao || '')}</td>
      <td class="crud-actions-cell">
        <button type="button" class="crud-mini-btn" data-action="edit-teste" data-id="${teste.idTeste}">Editar</button>
        <button type="button" class="crud-mini-btn danger" data-action="delete-teste" data-id="${teste.idTeste}">Excluir</button>
      </td>
    </tr>
  `).join('');
}

async function loadTestesCrud() {
  const data = await requestJson(`${API_BASE}/testes?limit=120`);
  state.testes = Array.isArray(data) ? data : [];
  renderTestesCrud();
}

async function handleTesteSubmit(event) {
  event.preventDefault();
  clearCrudMessage('testesCrudMsg');

  const id = document.getElementById('testeId').value;
  const payload = {
    nomeTeste: document.getElementById('testeNome').value,
    descricao: document.getElementById('testeDescricao').value
  };

  const isUpdate = Boolean(id);
  const url = isUpdate ? `${API_BASE}/testes/${id}` : `${API_BASE}/testes`;
  const method = isUpdate ? 'PUT' : 'POST';

  try {
    const result = await requestJson(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    showCrudMessage('testesCrudMsg', result?.message || 'Operacao realizada.', 'success');
    resetTesteForm();
    await loadTestesCrud();
    await refreshDatabasePanels('teste');
  } catch (error) {
    showCrudMessage('testesCrudMsg', error.message || 'Falha ao salvar teste.', 'error');
  }
}

async function handleTesteGridClick(event) {
  const button = event.target.closest('button[data-action]');
  if (!button) return;

  const id = Number(button.getAttribute('data-id'));
  const action = button.getAttribute('data-action');
  if (!id || !action) return;

  if (action === 'edit-teste') {
    const teste = state.testes.find(item => item.idTeste === id);
    if (teste) {
      fillTesteForm(teste);
      showCrudMessage('testesCrudMsg', `Editando teste ID ${id}.`, 'info');
    }
    return;
  }

  if (action === 'delete-teste') {
    const confirmar = window.confirm(`Deseja remover o teste ID ${id}?`);
    if (!confirmar) return;

    try {
      const result = await requestJson(`${API_BASE}/testes/${id}`, { method: 'DELETE' });
      showCrudMessage('testesCrudMsg', result?.message || 'Teste removido.', 'success');
      await loadTestesCrud();
      await refreshDatabasePanels('teste');
    } catch (error) {
      showCrudMessage('testesCrudMsg', error.message || 'Falha ao remover teste.', 'error');
    }
  }
}

function showGlobalError(message) {
  const wrap = document.getElementById('rowsGridWrap');
  if (wrap) {
    wrap.innerHTML = `<p class="db-error">${escapeHtml(message)}</p>`;
  }
}

function setupEvents() {
  const search = document.getElementById('tableSearch');
  const refresh = document.getElementById('btnRefresh');
  const limit = document.getElementById('rowLimit');
  const formUsuario = document.getElementById('formUsuario');
  const formTeste = document.getElementById('formTeste');
  const btnUsuarioClear = document.getElementById('btnUsuarioClear');
  const btnTesteClear = document.getElementById('btnTesteClear');
  const usuariosGridBody = document.querySelector('#usuariosCrudGrid tbody');
  const testesGridBody = document.querySelector('#testesCrudGrid tbody');

  search?.addEventListener('input', applyFilter);
  refresh?.addEventListener('click', async () => {
    try {
      await refreshDatabasePanels();
      await loadUsuariosCrud();
      await loadTestesCrud();
    } catch (error) {
      showGlobalError(error.message || 'Erro ao atualizar dados.');
    }
  });

  limit?.addEventListener('change', async () => {
    try {
      await loadTableDetails();
    } catch (error) {
      showGlobalError(error.message || 'Erro ao atualizar limite de linhas.');
    }
  });

  formUsuario?.addEventListener('submit', handleUsuarioSubmit);
  formTeste?.addEventListener('submit', handleTesteSubmit);
  btnUsuarioClear?.addEventListener('click', () => {
    resetUsuarioForm();
    clearCrudMessage('usuariosCrudMsg');
  });
  btnTesteClear?.addEventListener('click', () => {
    resetTesteForm();
    clearCrudMessage('testesCrudMsg');
  });
  usuariosGridBody?.addEventListener('click', handleUsuarioGridClick);
  testesGridBody?.addEventListener('click', handleTesteGridClick);
}

async function init() {
  setupEvents();
  updateCrudVisibility();

  const tasks = [loadOverview(), loadUsuariosCrud(), loadTestesCrud()];
  const results = await Promise.allSettled(tasks);
  const hasError = results.some(r => r.status === 'rejected');

  if (hasError) {
    showGlobalError('Falha ao carregar parte da interface. Tente atualizar.');
    results.forEach(result => {
      if (result.status === 'rejected') {
        console.error(result.reason);
      }
    });
  }
}

document.addEventListener('DOMContentLoaded', init);
