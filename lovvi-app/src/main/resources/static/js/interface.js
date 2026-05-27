const API_BASE = '/api/interface';

const state = {
  databaseName: '-',
  tables: [],
  filteredTables: [],
  selectedTableName: null,
  tableDetailsByName: new Map(),
  usuarios: [],
  testes: [],
  interesses: [],
  matches: [],
  relatorios: []
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
    payload = text ? { message: text } : null;
  }

  if (!response.ok) {
    throw new Error(payload?.message || `Erro ${response.status}`);
  }

  return payload;
}

function renderTableFromRows(containerId, rows, emptyMessage) {
  const wrap = document.getElementById(containerId);
  if (!wrap) return;

  if (!Array.isArray(rows) || rows.length === 0) {
    wrap.innerHTML = `<p class="db-empty">${escapeHtml(emptyMessage || 'Sem registros para exibir.')}</p>`;
    return;
  }

  const columns = Object.keys(rows[0]);
  const header = columns.map(col => `<th>${escapeHtml(col)}</th>`).join('');
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

function updateCrudVisibility() {
  const selectedTable = normalizeTableName(state.selectedTableName);
  const cards = {
    usuario: document.getElementById('crudUsuarioCard'),
    teste: document.getElementById('crudTesteCard'),
    interesse: document.getElementById('crudInteresseCard'),
    lovvi_match: document.getElementById('crudMatchCard')
  };
  const hint = document.getElementById('crudHint');
  let anyVisible = false;

  Object.entries(cards).forEach(([table, card]) => {
    if (!card) return;
    const visible = selectedTable === table;
    card.hidden = !visible;
    anyVisible = anyVisible || visible;
  });

  if (hint) {
    hint.hidden = anyVisible;
  }
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
      <span class="db-table-meta">${table.totalColumns} colunas - ${table.totalRows} registros</span>
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
      <td>${col.nullable ? 'Sim' : 'Nao'}</td>
    </tr>
  `).join('');
}

function renderRows(rows) {
  renderTableFromRows('rowsGridWrap', rows, 'Sem registros para exibir.');
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
  loadTableDetails().catch(error => showGlobalError(error.message || 'Erro ao carregar detalhes da tabela.'));
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

  await saveCrud({
    id,
    urlBase: `${API_BASE}/usuarios`,
    payload,
    messageId: 'usuariosCrudMsg',
    reset: resetUsuarioForm,
    reload: async () => {
      await loadUsuariosCrud();
      await refreshDatabasePanels('usuario');
    }
  });
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
    await deleteCrud(`${API_BASE}/usuarios/${id}`, 'usuariosCrudMsg', 'Usuario removido.', async () => {
      await loadUsuariosCrud();
      await refreshDatabasePanels('usuario');
    });
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

  await saveCrud({
    id,
    urlBase: `${API_BASE}/testes`,
    payload,
    messageId: 'testesCrudMsg',
    reset: resetTesteForm,
    reload: async () => {
      await loadTestesCrud();
      await refreshDatabasePanels('teste');
    }
  });
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
    await deleteCrud(`${API_BASE}/testes/${id}`, 'testesCrudMsg', 'Teste removido.', async () => {
      await loadTestesCrud();
      await refreshDatabasePanels('teste');
    });
  }
}

function fillInteresseForm(interesse) {
  document.getElementById('interesseId').value = interesse.idInteresse;
  document.getElementById('interesseNome').value = interesse.nomeInteresse || '';
  document.getElementById('interesseCategoria').value = interesse.categoria || '';
}

function resetInteresseForm() {
  document.getElementById('interesseId').value = '';
  document.getElementById('formInteresse')?.reset();
}

function renderInteressesCrud() {
  const tbody = document.querySelector('#interessesCrudGrid tbody');
  if (!tbody) return;

  if (!Array.isArray(state.interesses) || state.interesses.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4">Nenhum interesse encontrado.</td></tr>';
    return;
  }

  tbody.innerHTML = state.interesses.map(interesse => `
    <tr>
      <td>${interesse.idInteresse}</td>
      <td>${escapeHtml(interesse.nomeInteresse)}</td>
      <td>${escapeHtml(interesse.categoria)}</td>
      <td class="crud-actions-cell">
        <button type="button" class="crud-mini-btn" data-action="edit-interesse" data-id="${interesse.idInteresse}">Editar</button>
        <button type="button" class="crud-mini-btn danger" data-action="delete-interesse" data-id="${interesse.idInteresse}">Excluir</button>
      </td>
    </tr>
  `).join('');
}

async function loadInteressesCrud() {
  const data = await requestJson(`${API_BASE}/interesses?limit=120`);
  state.interesses = Array.isArray(data) ? data : [];
  renderInteressesCrud();
}

async function handleInteresseSubmit(event) {
  event.preventDefault();
  clearCrudMessage('interessesCrudMsg');

  const id = document.getElementById('interesseId').value;
  const payload = {
    nomeInteresse: document.getElementById('interesseNome').value,
    categoria: document.getElementById('interesseCategoria').value
  };

  await saveCrud({
    id,
    urlBase: `${API_BASE}/interesses`,
    payload,
    messageId: 'interessesCrudMsg',
    reset: resetInteresseForm,
    reload: async () => {
      await loadInteressesCrud();
      await refreshDatabasePanels('interesse');
    }
  });
}

async function handleInteresseGridClick(event) {
  const button = event.target.closest('button[data-action]');
  if (!button) return;

  const id = Number(button.getAttribute('data-id'));
  const action = button.getAttribute('data-action');
  if (!id || !action) return;

  if (action === 'edit-interesse') {
    const interesse = state.interesses.find(item => item.idInteresse === id);
    if (interesse) {
      fillInteresseForm(interesse);
      showCrudMessage('interessesCrudMsg', `Editando interesse ID ${id}.`, 'info');
    }
    return;
  }

  if (action === 'delete-interesse') {
    await deleteCrud(`${API_BASE}/interesses/${id}`, 'interessesCrudMsg', 'Interesse removido.', async () => {
      await loadInteressesCrud();
      await refreshDatabasePanels('interesse');
    });
  }
}

function fillMatchForm(match) {
  document.getElementById('matchId').value = match.idMatch;
  document.getElementById('matchUsuario1').value = match.idUsuario1;
  document.getElementById('matchUsuario2').value = match.idUsuario2;
  document.getElementById('matchCompatibilidade').value = match.compatibilidade ?? '';
  document.getElementById('matchStatus').value = match.statusMatch || 'pendente';
}

function resetMatchForm() {
  document.getElementById('matchId').value = '';
  document.getElementById('formMatch')?.reset();
}

function renderMatchesCrud() {
  const tbody = document.querySelector('#matchesCrudGrid tbody');
  if (!tbody) return;

  if (!Array.isArray(state.matches) || state.matches.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6">Nenhum match encontrado.</td></tr>';
    return;
  }

  tbody.innerHTML = state.matches.map(match => `
    <tr>
      <td>${match.idMatch}</td>
      <td>${match.idUsuario1} - ${escapeHtml(match.usuario1 || '')}</td>
      <td>${match.idUsuario2} - ${escapeHtml(match.usuario2 || '')}</td>
      <td>${formatValue(match.compatibilidade)}</td>
      <td>${escapeHtml(match.statusMatch || '')}</td>
      <td class="crud-actions-cell">
        <button type="button" class="crud-mini-btn" data-action="edit-match" data-id="${match.idMatch}">Editar</button>
        <button type="button" class="crud-mini-btn danger" data-action="delete-match" data-id="${match.idMatch}">Excluir</button>
      </td>
    </tr>
  `).join('');
}

async function loadMatchesCrud() {
  const data = await requestJson(`${API_BASE}/matches?limit=120`);
  state.matches = Array.isArray(data) ? data : [];
  renderMatchesCrud();
}

async function handleMatchSubmit(event) {
  event.preventDefault();
  clearCrudMessage('matchesCrudMsg');

  const id = document.getElementById('matchId').value;
  const payload = {
    idUsuario1: Number(document.getElementById('matchUsuario1').value),
    idUsuario2: Number(document.getElementById('matchUsuario2').value),
    compatibilidade: Number(document.getElementById('matchCompatibilidade').value),
    statusMatch: document.getElementById('matchStatus').value
  };

  await saveCrud({
    id,
    urlBase: `${API_BASE}/matches`,
    payload,
    messageId: 'matchesCrudMsg',
    reset: resetMatchForm,
    reload: async () => {
      await loadMatchesCrud();
      await refreshDatabasePanels('lovvi_match');
      await loadLogs();
    }
  });
}

async function handleMatchGridClick(event) {
  const button = event.target.closest('button[data-action]');
  if (!button) return;

  const id = Number(button.getAttribute('data-id'));
  const action = button.getAttribute('data-action');
  if (!id || !action) return;

  if (action === 'edit-match') {
    const match = state.matches.find(item => item.idMatch === id);
    if (match) {
      fillMatchForm(match);
      showCrudMessage('matchesCrudMsg', `Editando match ID ${id}.`, 'info');
    }
    return;
  }

  if (action === 'delete-match') {
    await deleteCrud(`${API_BASE}/matches/${id}`, 'matchesCrudMsg', 'Match removido.', async () => {
      await loadMatchesCrud();
      await refreshDatabasePanels('lovvi_match');
    });
  }
}

async function saveCrud({ id, urlBase, payload, messageId, reset, reload }) {
  const isUpdate = Boolean(id);
  const url = isUpdate ? `${urlBase}/${id}` : urlBase;
  const method = isUpdate ? 'PUT' : 'POST';

  try {
    const result = await requestJson(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    showCrudMessage(messageId, result?.message || 'Operacao realizada.', 'success');
    reset();
    await reload();
  } catch (error) {
    showCrudMessage(messageId, error.message || 'Falha ao salvar.', 'error');
  }
}

async function deleteCrud(url, messageId, successMessage, reload) {
  const confirmar = window.confirm('Deseja remover este registro?');
  if (!confirmar) return;

  try {
    const result = await requestJson(url, { method: 'DELETE' });
    showCrudMessage(messageId, result?.message || successMessage, 'success');
    await reload();
  } catch (error) {
    showCrudMessage(messageId, error.message || 'Falha ao remover.', 'error');
  }
}

async function loadRelatorios() {
  const select = document.getElementById('relatorioSelect');
  if (!select) return;

  const data = await requestJson(`${API_BASE}/relatorios`);
  state.relatorios = Array.isArray(data) ? data : [];
  select.innerHTML = state.relatorios.map(item => `
    <option value="${escapeHtml(item.id)}">${escapeHtml(item.titulo)}</option>
  `).join('');

  await runRelatorio();
}

async function runRelatorio() {
  const id = document.getElementById('relatorioSelect')?.value;
  if (!id) return;

  clearCrudMessage('relatorioMsg');
  const params = new URLSearchParams();
  params.set('cidade', document.getElementById('relatorioCidade')?.value || '');
  params.set('tipoPerfil', document.getElementById('relatorioTipoPerfil')?.value || '');
  params.set('categoria', document.getElementById('relatorioCategoria')?.value || '');
  params.set('status', document.getElementById('relatorioStatus')?.value || '');
  params.set('limit', document.getElementById('relatorioLimit')?.value || '50');

  try {
    const details = await requestJson(`${API_BASE}/relatorios/${encodeURIComponent(id)}?${params}`);
    renderTableFromRows('relatorioResult', details.rows, 'Relatorio sem dados.');
    const relatorio = state.relatorios.find(item => item.id === id);
    if (relatorio) {
      showCrudMessage('relatorioMsg', relatorio.descricao, 'info');
    }
  } catch (error) {
    showCrudMessage('relatorioMsg', error.message || 'Falha ao executar relatorio.', 'error');
  }
}

async function handleFnIdade(event) {
  event.preventDefault();
  try {
    const result = await requestJson(`${API_BASE}/funcoes/idade`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ dtNascimento: document.getElementById('fnNascimento').value })
    });
    setText('fnIdadeResult', result.valor === null ? 'idade invalida' : `${result.valor} anos`);
  } catch (error) {
    setText('fnIdadeResult', error.message || 'erro');
  }
}

async function handleFnCompat(event) {
  event.preventDefault();
  try {
    const result = await requestJson(`${API_BASE}/funcoes/compatibilidade`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        idUsuario1: Number(document.getElementById('fnUsuario1').value),
        idUsuario2: Number(document.getElementById('fnUsuario2').value)
      })
    });
    setText('fnCompatResult', `${result.valor ?? 0}%`);
  } catch (error) {
    setText('fnCompatResult', error.message || 'erro');
  }
}

async function handleProcStatus(event) {
  event.preventDefault();
  try {
    const result = await requestJson(`${API_BASE}/procedimentos/match-status`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        idMatch: Number(document.getElementById('procMatchId').value),
        statusMatch: document.getElementById('procStatus').value
      })
    });
    setText('procStatusResult', result.message || 'procedimento executado');
    await loadMatchesCrud();
    await refreshDatabasePanels('lovvi_match');
    await loadLogs();
  } catch (error) {
    setText('procStatusResult', error.message || 'erro');
  }
}

async function loadLogs() {
  try {
    const details = await requestJson(`${API_BASE}/logs/matches?limit=20`);
    renderTableFromRows('logsResult', details.rows, 'Nenhum log encontrado.');
  } catch (error) {
    renderTableFromRows('logsResult', [], error.message || 'Falha ao carregar logs.');
  }
}

async function loadDashboard() {
  const metricsWrap = document.getElementById('dashboardMetrics');
  const chartsWrap = document.getElementById('dashboardCharts');
  if (!metricsWrap || !chartsWrap) return;

  try {
    const data = await requestJson(`${API_BASE}/dashboard`);
    metricsWrap.innerHTML = (data.metrics || []).map(metric => `
      <article class="dashboard-metric">
        <span>${escapeHtml(metric.label)}</span>
        <strong>${escapeHtml(metric.value)}</strong>
      </article>
    `).join('');

    chartsWrap.innerHTML = (data.charts || []).map(chart => renderDashboardChart(chart)).join('');
  } catch (error) {
    metricsWrap.innerHTML = `<p class="db-error">${escapeHtml(error.message || 'Falha ao carregar dashboard.')}</p>`;
    chartsWrap.innerHTML = '';
  }
}

function renderDashboardChart(chart) {
  const points = Array.isArray(chart.points) ? chart.points : [];
  const max = points.reduce((current, point) => Math.max(current, Number(point.value) || 0), 0) || 1;
  const rows = points.map(point => {
    const value = Number(point.value) || 0;
    const width = Math.max(4, (value / max) * 100);
    return `
      <div class="chart-row">
        <span>${escapeHtml(point.label)}</span>
        <div class="chart-track"><i style="width:${width}%"></i></div>
        <strong>${escapeHtml(point.value)}</strong>
      </div>
    `;
  }).join('');

  return `
    <article class="dashboard-chart">
      <h3>${escapeHtml(chart.title)}</h3>
      <div>${rows || '<p class="db-empty">Sem dados.</p>'}</div>
    </article>
  `;
}

function showGlobalError(message) {
  const wrap = document.getElementById('rowsGridWrap');
  if (wrap) {
    wrap.innerHTML = `<p class="db-error">${escapeHtml(message)}</p>`;
  }
}

function setupEvents() {
  document.getElementById('tableSearch')?.addEventListener('input', applyFilter);
  document.getElementById('rowLimit')?.addEventListener('change', () => {
    loadTableDetails().catch(error => showGlobalError(error.message || 'Erro ao atualizar limite de linhas.'));
  });

  document.getElementById('btnRefresh')?.addEventListener('click', async () => {
    try {
      await refreshAllData();
    } catch (error) {
      showGlobalError(error.message || 'Erro ao atualizar dados.');
    }
  });

  document.getElementById('formUsuario')?.addEventListener('submit', handleUsuarioSubmit);
  document.getElementById('formTeste')?.addEventListener('submit', handleTesteSubmit);
  document.getElementById('formInteresse')?.addEventListener('submit', handleInteresseSubmit);
  document.getElementById('formMatch')?.addEventListener('submit', handleMatchSubmit);
  document.getElementById('formFnIdade')?.addEventListener('submit', handleFnIdade);
  document.getElementById('formFnCompat')?.addEventListener('submit', handleFnCompat);
  document.getElementById('formProcStatus')?.addEventListener('submit', handleProcStatus);

  document.getElementById('btnUsuarioClear')?.addEventListener('click', () => {
    resetUsuarioForm();
    clearCrudMessage('usuariosCrudMsg');
  });
  document.getElementById('btnTesteClear')?.addEventListener('click', () => {
    resetTesteForm();
    clearCrudMessage('testesCrudMsg');
  });
  document.getElementById('btnInteresseClear')?.addEventListener('click', () => {
    resetInteresseForm();
    clearCrudMessage('interessesCrudMsg');
  });
  document.getElementById('btnMatchClear')?.addEventListener('click', () => {
    resetMatchForm();
    clearCrudMessage('matchesCrudMsg');
  });

  document.querySelector('#usuariosCrudGrid tbody')?.addEventListener('click', handleUsuarioGridClick);
  document.querySelector('#testesCrudGrid tbody')?.addEventListener('click', handleTesteGridClick);
  document.querySelector('#interessesCrudGrid tbody')?.addEventListener('click', handleInteresseGridClick);
  document.querySelector('#matchesCrudGrid tbody')?.addEventListener('click', handleMatchGridClick);

  document.getElementById('btnRunRelatorio')?.addEventListener('click', runRelatorio);
  document.getElementById('btnLoadLogs')?.addEventListener('click', loadLogs);
}

async function refreshAllData() {
  const tasks = [];

  if (document.getElementById('tableList')) {
    tasks.push(loadOverview());
  }
  if (document.getElementById('usuariosCrudGrid')) {
    tasks.push(loadUsuariosCrud(), loadTestesCrud(), loadInteressesCrud(), loadMatchesCrud());
  }
  if (document.getElementById('relatorioSelect')) {
    tasks.push(loadRelatorios());
  }
  if (document.getElementById('logsResult')) {
    tasks.push(loadLogs());
  }
  if (document.getElementById('dashboardMetrics')) {
    tasks.push(loadDashboard());
  }

  await Promise.allSettled(tasks);
}

async function init() {
  setupEvents();
  updateCrudVisibility();

  const tasks = [];

  if (document.getElementById('tableList')) {
    tasks.push(loadOverview());
  }
  if (document.getElementById('usuariosCrudGrid')) {
    tasks.push(loadUsuariosCrud(), loadTestesCrud(), loadInteressesCrud(), loadMatchesCrud());
  }
  if (document.getElementById('relatorioSelect')) {
    tasks.push(loadRelatorios());
  }
  if (document.getElementById('logsResult')) {
    tasks.push(loadLogs());
  }
  if (document.getElementById('dashboardMetrics')) {
    tasks.push(loadDashboard());
  }

  const results = await Promise.allSettled(tasks);

  results.forEach(result => {
    if (result.status === 'rejected') {
      console.error(result.reason);
    }
  });
}

document.addEventListener('DOMContentLoaded', init);
