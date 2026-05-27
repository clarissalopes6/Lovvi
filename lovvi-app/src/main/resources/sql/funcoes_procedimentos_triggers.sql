USE lovvi_db;

DROP FUNCTION IF EXISTS fn_calcular_idade_usuario;
DROP FUNCTION IF EXISTS fn_calcular_compatibilidade;
DROP PROCEDURE IF EXISTS sp_atualizar_status_match;
DROP PROCEDURE IF EXISTS sp_gerar_matches_usuario;
DROP TRIGGER IF EXISTS trg_usuario_bi_valida_maioridade;
DROP TRIGGER IF EXISTS trg_match_au_log_status;

CREATE TABLE IF NOT EXISTS log_match (
    id_log                    INT NOT NULL AUTO_INCREMENT,
    id_match                  INT NOT NULL,
    id_usuario_1              INT NOT NULL,
    id_usuario_2              INT NOT NULL,
    status_anterior           VARCHAR(20),
    status_novo               VARCHAR(20),
    compatibilidade_anterior  DECIMAL(5,2),
    compatibilidade_nova      DECIMAL(5,2),
    operacao                  VARCHAR(30) NOT NULL,
    data_log                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_log_match PRIMARY KEY (id_log)
);

DELIMITER //

/* Calcula a idade a partir da data de nascimento.
   A regra tambem ajuda a validar a maioridade do usuario.
*/
CREATE FUNCTION fn_calcular_idade_usuario(p_dt_nascimento DATE)
RETURNS INT
NOT DETERMINISTIC
NO SQL
BEGIN
    DECLARE v_idade INT;

    IF p_dt_nascimento IS NULL OR p_dt_nascimento > CURRENT_DATE() THEN
        RETURN NULL;
    END IF;

    SET v_idade = TIMESTAMPDIFF(YEAR, p_dt_nascimento, CURRENT_DATE());

    IF v_idade < 0 THEN
        RETURN NULL;
    END IF;

    RETURN v_idade;
END //

/* Calcula a compatibilidade com a mesma regra usada pela tela de match:
   perfil igual, cidade igual, faixa etaria proxima e proporcao de interesses em comum.
*/
CREATE FUNCTION fn_calcular_compatibilidade(
    p_id_usuario_1 INT,
    p_id_usuario_2 INT
)
RETURNS DECIMAL(5,2)
NOT DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_cidade_1             VARCHAR(100);
    DECLARE v_cidade_2             VARCHAR(100);
    DECLARE v_tipo_perfil_1        VARCHAR(50);
    DECLARE v_tipo_perfil_2        VARCHAR(50);
    DECLARE v_dt_nascimento_1      DATE;
    DECLARE v_dt_nascimento_2      DATE;
    DECLARE v_idade_1              INT DEFAULT NULL;
    DECLARE v_idade_2              INT DEFAULT NULL;
    DECLARE v_diferenca_idade      INT DEFAULT NULL;
    DECLARE v_total_interesses_1   INT DEFAULT 0;
    DECLARE v_total_interesses_2   INT DEFAULT 0;
    DECLARE v_interesses_comuns    INT DEFAULT 0;
    DECLARE v_score                DECIMAL(6,2) DEFAULT 0.00;

    IF p_id_usuario_1 IS NULL
       OR p_id_usuario_2 IS NULL
       OR p_id_usuario_1 = p_id_usuario_2 THEN
        RETURN 0.00;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = p_id_usuario_1)
       OR NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = p_id_usuario_2) THEN
        RETURN 0.00;
    END IF;

    SELECT u.cidade, p.tipo_perfil, u.dt_nascimento
      INTO v_cidade_1, v_tipo_perfil_1, v_dt_nascimento_1
      FROM usuario u
      LEFT JOIN perfil p ON p.id_usuario = u.id_usuario
     WHERE u.id_usuario = p_id_usuario_1;

    SELECT u.cidade, p.tipo_perfil, u.dt_nascimento
      INTO v_cidade_2, v_tipo_perfil_2, v_dt_nascimento_2
      FROM usuario u
      LEFT JOIN perfil p ON p.id_usuario = u.id_usuario
     WHERE u.id_usuario = p_id_usuario_2;

    IF v_tipo_perfil_1 IS NOT NULL
       AND v_tipo_perfil_2 IS NOT NULL
       AND LOWER(v_tipo_perfil_1) = LOWER(v_tipo_perfil_2) THEN
        SET v_score = v_score + 30.00;
    END IF;

    IF v_cidade_1 IS NOT NULL
       AND v_cidade_2 IS NOT NULL
       AND LOWER(v_cidade_1) = LOWER(v_cidade_2) THEN
        SET v_score = v_score + 15.00;
    END IF;

    IF v_dt_nascimento_1 IS NOT NULL AND v_dt_nascimento_2 IS NOT NULL THEN
        SET v_idade_1 = TIMESTAMPDIFF(YEAR, v_dt_nascimento_1, CURRENT_DATE());
        SET v_idade_2 = TIMESTAMPDIFF(YEAR, v_dt_nascimento_2, CURRENT_DATE());
        SET v_diferenca_idade = ABS(v_idade_1 - v_idade_2);

        IF v_diferenca_idade <= 5 THEN
            SET v_score = v_score + 15.00;
        ELSEIF v_diferenca_idade <= 10 THEN
            SET v_score = v_score + 8.00;
        END IF;
    END IF;

    SELECT COUNT(*)
      INTO v_total_interesses_1
      FROM tem
     WHERE id_usuario = p_id_usuario_1;

    SELECT COUNT(*)
      INTO v_total_interesses_2
      FROM tem
     WHERE id_usuario = p_id_usuario_2;

    IF v_total_interesses_1 > 0 AND v_total_interesses_2 > 0 THEN
        SELECT COUNT(*)
          INTO v_interesses_comuns
          FROM tem t1
          JOIN tem t2 ON t2.id_interesse = t1.id_interesse
         WHERE t1.id_usuario = p_id_usuario_1
           AND t2.id_usuario = p_id_usuario_2;

        SET v_score = v_score + LEAST(
            40.00,
            (v_interesses_comuns / GREATEST(v_total_interesses_1, v_total_interesses_2)) * 40.00
        );
    END IF;

    IF v_score > 100 THEN
        SET v_score = 100.00;
    END IF;

    RETURN ROUND(v_score, 2);
END //

/* Atualiza o status do match mantendo a lista de status aceita pelo sistema. */
CREATE PROCEDURE sp_atualizar_status_match(
    IN p_id_match INT,
    IN p_novo_status VARCHAR(20)
)
BEGIN
    DECLARE v_status VARCHAR(20);

    SET v_status = LOWER(TRIM(p_novo_status));

    IF v_status NOT IN ('pendente', 'aceito', 'recusado') THEN
        SIGNAL SQLSTATE '45003'
            SET MESSAGE_TEXT = 'Status invalido para o match.';
    END IF;

    UPDATE lovvi_match
       SET status_match = v_status
     WHERE id_match = p_id_match;

    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45003'
            SET MESSAGE_TEXT = 'Match nao encontrado.';
    END IF;
END //

/* Percorre candidatos e gera matches para um usuario quando a pontuacao minima
   de compatibilidade for atingida.
*/
CREATE PROCEDURE sp_gerar_matches_usuario(
    IN p_id_usuario INT,
    IN p_compatibilidade_minima DECIMAL(5,2)
)
BEGIN
    DECLARE v_fim                 BOOLEAN DEFAULT FALSE;
    DECLARE v_id_candidato        INT;
    DECLARE v_score               DECIMAL(5,2) DEFAULT 0.00;
    DECLARE v_total_inseridos     INT DEFAULT 0;

    DECLARE cur_candidatos CURSOR FOR
        SELECT u.id_usuario
          FROM usuario u
         WHERE u.id_usuario <> p_id_usuario
           AND NOT EXISTS (
               SELECT 1
                 FROM lovvi_match m
                WHERE (m.id_usuario_1 = p_id_usuario AND m.id_usuario_2 = u.id_usuario)
                   OR (m.id_usuario_1 = u.id_usuario AND m.id_usuario_2 = p_id_usuario)
           )
         ORDER BY u.id_usuario;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_fim = TRUE;

    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = p_id_usuario) THEN
        SIGNAL SQLSTATE '45003'
            SET MESSAGE_TEXT = 'Usuario nao encontrado.';
    END IF;

    IF p_compatibilidade_minima IS NULL OR p_compatibilidade_minima < 0 THEN
        SET p_compatibilidade_minima = 50.00;
    END IF;

    OPEN cur_candidatos;

    leitura_candidatos: LOOP
        FETCH cur_candidatos INTO v_id_candidato;

        IF v_fim THEN
            LEAVE leitura_candidatos;
        END IF;

        SET v_score = fn_calcular_compatibilidade(p_id_usuario, v_id_candidato);

        IF v_score >= p_compatibilidade_minima THEN
            INSERT INTO lovvi_match (
                id_usuario_1,
                id_usuario_2,
                data_match,
                compatibilidade,
                status_match
            ) VALUES (
                p_id_usuario,
                v_id_candidato,
                CURRENT_DATE(),
                v_score,
                'pendente'
            );

            SET v_total_inseridos = v_total_inseridos + 1;
        END IF;
    END LOOP;

    CLOSE cur_candidatos;

    SELECT v_total_inseridos AS total_matches_gerados;
END //

/* Impede cadastro de menor de idade ou com data de nascimento futura. */
CREATE TRIGGER trg_usuario_bi_valida_maioridade
BEFORE INSERT ON usuario
FOR EACH ROW
BEGIN
    DECLARE v_idade INT;

    IF NEW.dt_nascimento IS NULL OR NEW.dt_nascimento > CURRENT_DATE() THEN
        SIGNAL SQLSTATE '45003'
            SET MESSAGE_TEXT = 'Data de nascimento invalida.';
    END IF;

    SET v_idade = TIMESTAMPDIFF(YEAR, NEW.dt_nascimento, CURRENT_DATE());

    IF v_idade < 18 THEN
        SIGNAL SQLSTATE '45003'
            SET MESSAGE_TEXT = 'Usuario deve ter pelo menos 18 anos.';
    END IF;
END //

/* Registra mudancas de status ou compatibilidade dos matches. */
CREATE TRIGGER trg_match_au_log_status
AFTER UPDATE ON lovvi_match
FOR EACH ROW
BEGIN
    IF NOT (OLD.status_match <=> NEW.status_match)
       OR NOT (OLD.compatibilidade <=> NEW.compatibilidade) THEN
        INSERT INTO log_match (
            id_match,
            id_usuario_1,
            id_usuario_2,
            status_anterior,
            status_novo,
            compatibilidade_anterior,
            compatibilidade_nova,
            operacao
        ) VALUES (
            NEW.id_match,
            NEW.id_usuario_1,
            NEW.id_usuario_2,
            OLD.status_match,
            NEW.status_match,
            OLD.compatibilidade,
            NEW.compatibilidade,
            'UPDATE_MATCH'
        );
    END IF;
END //

DELIMITER ;
