# Lovvi: Plataforma de relacionamento

O **Lovvi** é um projeto de plataforma de relacionamentos focado na precisão do cruzamento de dados. Diferente de apps baseados apenas em fotos, o Lovvi utiliza o perfil comportamental e as preferências declaradas para gerar conexões reais através de consultas diretas no banco de dados!

---

## O Conceito
A ideia central do projeto é transformar afinidade em dados estruturados. O usuário não apenas se cadastra, ele passa por um **Teste de Perfil** que alimenta tabelas de características e gostos. O "Match" não é um clique aleatório, mas sim o resultado do cruzamento dessas tabelas.

### Como funciona:
1. **Cadastro:** Registro básico de conta.
2. **Teste:** O usuário responde sobre seu estilo de vida e o que procura.
3. **Match:** O sistema varre o banco de dados buscando quem respondeu de forma compatível aos interesses.

---

## Estrutura de Dados

O projeto foca na relação entre três pilares principais:

* **`usuario`**: Armazena o núcleo do perfil (nome, email, gênero, data de nascimento).
* **`perfil`**: Detalhes específicos como descrição, preferências gerais e altura.
* **`pergunta`**: Sistema dinâmico de questionários divididos por categorias.
* **`resposta`**: O vínculo crucial que liga o usuário às escolhas feitas no teste.
* **`interesse`**: Tabela de tags (hobbies/atividades) que o usuário possui.
* **`usuario_relacionamento_serio` / `usuario_amizade`**: Especializações que definem o objetivo do usuário e filtros como distância máxima.
* **`match`**: Registra a compatibilidade e o status da conexão gerada pelo cruzamento.
---

## Tecnologias Utilizadas
* **Linguagem:** Java (Springboot)
* **Banco de Dados:** MySQL
* **Interface:** HTML/CSS/JS

---
