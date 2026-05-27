USE lovvi_db;

/* Consultas, views e indices usados nos relatorios do Lovvi.
   As consultas abaixo servem como base para a tela administrativa.
*/

/* Categorias de interesse mais presentes entre os usuarios. */
SELECT i.categoria, COUNT(t.id_usuario) AS total_usuarios
  FROM interesse i
  JOIN tem t ON i.id_interesse = t.id_interesse
 GROUP BY i.categoria
HAVING COUNT(t.id_usuario) > 2;

/* Usuarios de Recife com dados de perfil e interesses. */
SELECT u.nome, p.descricao, i.nome_interesse
  FROM usuario u
  JOIN perfil p ON u.id_usuario = p.id_usuario
  JOIN tem t ON u.id_usuario = t.id_usuario
  JOIN interesse i ON t.id_interesse = i.id_interesse
 WHERE u.cidade = 'Recife';

/* Usuarios que ainda nao possuem perfil cadastrado. */
SELECT u.nome, u.email
  FROM usuario u
  LEFT JOIN perfil p ON u.id_usuario = p.id_usuario
 WHERE p.id_perfil IS NULL;

/* Matches com compatibilidade acima da media geral. */
SELECT id_usuario_1, id_usuario_2, compatibilidade
  FROM lovvi_match
 WHERE compatibilidade > (SELECT AVG(compatibilidade) FROM lovvi_match);

DROP VIEW IF EXISTS v_perfil_completo_usuario;
DROP VIEW IF EXISTS v_usuarios_destaque;

/* Perfil completo com usuario, perfil e interesses em uma consulta pronta.
   Evita repetir os mesmos joins nas telas de relatorio.
*/
CREATE VIEW v_perfil_completo_usuario AS
SELECT
    u.nome,
    u.cidade,
    p.tipo_perfil,
    p.objetivos,
    i.nome_interesse,
    i.categoria AS categoria_interesse
  FROM usuario u
  JOIN perfil p ON u.id_usuario = p.id_usuario
  JOIN tem t ON u.id_usuario = t.id_usuario
  JOIN interesse i ON t.id_interesse = i.id_interesse
 WHERE p.tipo_perfil IS NOT NULL;

/* Usuarios que tiveram pelo menos um match acima da media geral. */
CREATE VIEW v_usuarios_destaque AS
SELECT
    u.nome,
    u.email,
    MAX(m.compatibilidade) AS maior_compatibilidade_alcancada
  FROM usuario u
  JOIN lovvi_match m ON u.id_usuario = m.id_usuario_1
 GROUP BY u.id_usuario, u.nome, u.email
HAVING MAX(m.compatibilidade) > (
    SELECT AVG(compatibilidade)
      FROM lovvi_match
);

SET @idx_usuario_cidade_exists = (
    SELECT COUNT(1)
      FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = 'usuario'
       AND index_name = 'idx_usuario_cidade'
);
SET @sql_idx_usuario_cidade = IF(
    @idx_usuario_cidade_exists = 0,
    'CREATE INDEX idx_usuario_cidade ON usuario(cidade)',
    'SELECT ''idx_usuario_cidade ja existe'' AS aviso'
);
PREPARE stmt_idx_usuario_cidade FROM @sql_idx_usuario_cidade;
EXECUTE stmt_idx_usuario_cidade;
DEALLOCATE PREPARE stmt_idx_usuario_cidade;
/* Cidade aparece com frequencia nos filtros de busca. */

SET @idx_match_compatibilidade_exists = (
    SELECT COUNT(1)
      FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = 'lovvi_match'
       AND index_name = 'idx_match_compatibilidade'
);
SET @sql_idx_match_compatibilidade = IF(
    @idx_match_compatibilidade_exists = 0,
    'CREATE INDEX idx_match_compatibilidade ON lovvi_match(compatibilidade)',
    'SELECT ''idx_match_compatibilidade ja existe'' AS aviso'
);
PREPARE stmt_idx_match_compatibilidade FROM @sql_idx_match_compatibilidade;
EXECUTE stmt_idx_match_compatibilidade;
DEALLOCATE PREPARE stmt_idx_match_compatibilidade;
/* Compatibilidade e usada em filtros, medias e ordenacoes. */
