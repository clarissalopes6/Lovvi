const API_BASE = '/api';

function setMessage(containerId, message, type = 'info') {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.textContent = message;
  container.className = `message ${type}`;
}

function clearMessage(containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.textContent = '';
  container.className = '';
}

async function fetchInteresses() {
  const container = document.getElementById('interessesGrid');
  if (!container) return;

  try {
    const response = await fetch(`${API_BASE}/interesses`);
    if (!response.ok) throw new Error('Falha ao carregar interesses');
    const interesses = await response.json();

    container.innerHTML = interesses.length > 0 ? interesses.map(i => {
      return `
        <label class="checkbox-item">
          <input type="checkbox" name="interesse" value="${i.idInteresse}"> ${i.nomeInteresse}
        </label>
      `;
    }).join('') : '<p>Nenhum interesse disponível.</p>';
  } catch (error) {
    container.innerHTML = '<p>Erro ao carregar interesses. Tente novamente mais tarde.</p>';
    console.error(error);
  }
}

function getSelectedInterestIds() {
  return Array.from(document.querySelectorAll('#interessesGrid input[name="interesse"]:checked')).map(input => Number(input.value)).filter(n => !Number.isNaN(n));
}

async function fetchMatches(idUsuario) {
  const matchesList = document.getElementById('matchList');
  if (!matchesList) return;
  matchesList.innerHTML = '<p>Buscando matches...</p>';

  try {
    const response = await fetch(`${API_BASE}/usuarios/${idUsuario}/matches`);
    if (!response.ok) throw new Error('Falha ao buscar matches');
    const matches = await response.json();

    if (!Array.isArray(matches) || matches.length === 0) {
      matchesList.innerHTML = '<p>Nenhum match encontrado ainda. Tente expandir seus interesses ou localidade.</p>';
      return;
    }

    matchesList.innerHTML = matches.map(m => `
      <article class="match-card">
        <h4>${m.nomeCompleto} (<span class="compatibility">${m.compatibilidade}%</span>)</h4>
        <p><strong>Cidade:</strong> ${m.cidade} | <strong>Tipo:</strong> ${m.tipoPerfil}</p>
        <p><strong>Interesses em comum:</strong> ${m.interesses && m.interesses.length ? m.interesses.join(', ') : 'Nenhum'}</p>
      </article>
    `).join('');
  } catch (error) {
    matchesList.innerHTML = '<p>Erro ao buscar matches. Verifique o console.</p>';
    console.error(error);
  }
}

function normalizeGender(value) {
  const map = { M: 'Masculino', F: 'Feminino', O: 'Outro' };
  return map[value] || value;
}

function normalizeTipoPerfil(value) {
  if (value === 'serio' || value === 'relacionamento') return 'relacionamento';
  if (value === 'amizade') return 'amizade';
  if (value === 'casual') return 'casual';
  return value;
}

async function handleCadastro(event) {
  event.preventDefault();
  clearMessage('formMsg');

  const form = event.target;
  const alturaValue = form.altura?.value?.trim();

  const payload = {
    nome: form.nome?.value?.trim() || '',
    sobrenome: form.sobrenome?.value?.trim() || '',
    email: form.email?.value?.trim() || '',
    senha: form.senha?.value?.trim() || '',
    cidade: form.cidade?.value?.trim() || '',
    genero: normalizeGender(form.genero?.value || ''),
    dtNascimento: form.dt_nascimento?.value || '',
    descricao: form.descricao?.value?.trim() || '',
    preferencias: form.preferencias?.value?.trim() || '',
    objetivos: form.objetivos?.value?.trim() || '',
    tipoPerfil: normalizeTipoPerfil(form.tipo_perfil?.value || ''),
    altura: alturaValue ? Number(alturaValue) : null,
    interesses: getSelectedInterestIds()
  };

  if (!payload.nome || !payload.sobrenome || !payload.email || !payload.senha || !payload.cidade || !payload.genero || !payload.dtNascimento || !payload.tipoPerfil) {
    setMessage('formMsg', 'Preencha todos os campos obrigatórios corretamente.', 'error');
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/usuarios/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const body = await response.text();
      throw new Error(body || `Erro ao registrar usuário (código ${response.status})`);
    }

    const result = await response.json();
    setMessage('formMsg', `Usuário cadastrado com sucesso! ID ${result.idUsuario}`, 'success');
    await fetchMatches(result.idUsuario);
  } catch (error) {
    setMessage('formMsg', 'Falha no cadastro: ' + (error.message || 'verifique o console'), 'error');
    console.error(error);
  }
}

function initUI() {
  const cadastroForm = document.getElementById('formCadastro');
  if (cadastroForm) {
    cadastroForm.addEventListener('submit', handleCadastro);
  }

  fetchInteresses();

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.style.animation = 'fadeInUp 0.6s ease forwards';
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1 });

  document.querySelectorAll('.card, .about-card, .match-card').forEach(el => {
    el.style.opacity = '0';
    observer.observe(el);
  });

  const navbar = document.querySelector('.navbar');
  window.addEventListener('scroll', () => {
    if (!navbar) return;
    navbar.style.background = window.scrollY > 50 ? 'rgba(255, 250, 250, 0.98)' : 'rgba(255, 250, 250, 0.9)';
  });

  const heroContent = document.querySelector('.hero-content');
  if (heroContent) {
    heroContent.style.opacity = '0';
    heroContent.style.transform = 'translateY(20px)';
    heroContent.style.transition = 'all 0.8s ease';
    setTimeout(() => {
      heroContent.style.opacity = '1';
      heroContent.style.transform = 'translateY(0)';
    }, 100);
  }
}

document.addEventListener('DOMContentLoaded', initUI);
