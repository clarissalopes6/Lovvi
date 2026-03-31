INSERT INTO usuario (nome, sobrenome, email, senha, cidade, genero, dt_nascimento) VALUES
('Joao',      'Silva',      'joao.silva@email.com',      'senha123', 'Sao Paulo',      'Masculino', '1995-03-15'),
('Beatriz',   'Santos',     'beatriz.santos@email.com',  'senha123', 'Rio de Janeiro',  'Feminino',  '1998-07-22'),
('Carlos',    'Oliveira',   'carlos.oliveira@email.com', 'senha123', 'Belo Horizonte',  'Masculino', '1993-11-08'),
('Ana',       'Lima',       'ana.lima@email.com',        'senha123', 'Curitiba',        'Feminino',  '2000-01-30'),
('Pedro',     'Costa',      'pedro.costa@email.com',     'senha123', 'Sao Paulo',       'Masculino', '1997-05-18'),
('Fernanda',  'Rocha',      'fernanda.rocha@email.com',  'senha123', 'Porto Alegre',    'Feminino',  '1996-09-25'),
('Lucas',     'Ferreira',   'lucas.ferreira@email.com',  'senha123', 'Salvador',        'Masculino', '1999-12-04'),
('Marina',    'Alves',      'marina.alves@email.com',    'senha123', 'Fortaleza',       'Feminino',  '1994-06-14'),
('Gabriel',   'Melo',       'gabriel.melo@email.com',    'senha123', 'Manaus',          'Masculino', '2001-02-28'),
('Camila',    'Souza',      'camila.souza@email.com',    'senha123', 'Recife',          'Feminino',  '1997-08-17'),
('Rafael',    'Carvalho',   'rafael.carvalho@email.com', 'senha123', 'Fortaleza',       'Masculino', '1995-04-10'),
('Julia',     'Martins',    'julia.martins@email.com',   'senha123', 'Sao Paulo',       'Feminino',  '1999-03-05'),
('Diego',     'Barbosa',    'diego.barbosa@email.com',   'senha123', 'Rio de Janeiro',  'Masculino', '1992-07-19'),
('Larissa',   'Pereira',    'larissa.pereira@email.com', 'senha123', 'Campinas',        'Feminino',  '2000-11-23'),
('Thiago',    'Ribeiro',    'thiago.ribeiro@email.com',  'senha123', 'Brasilia',        'Masculino', '1996-01-12'),
('Natalia',   'Gomes',      'natalia.gomes@email.com',   'senha123', 'Curitiba',        'Feminino',  '1998-05-30'),
('Felipe',    'Araujo',     'felipe.araujo@email.com',   'senha123', 'Goiania',         'Masculino', '1994-09-07'),
('Isabella',  'Dias',       'isabella.dias@email.com',   'senha123', 'Belo Horizonte',  'Feminino',  '2001-04-15'),
('Matheus',   'Rodrigues',  'matheus.rodrigues@email.com','senha123','Vitoria',         'Masculino', '1997-10-21'),
('Sofia',     'Moreira',    'sofia.moreira@email.com',   'senha123', 'Manaus',          'Feminino',  '1995-12-18'),
('Eduardo',   'Nunes',      'eduardo.nunes@email.com',   'senha123', 'Recife',          'Masculino', '1993-06-03'),
('Valentina', 'Cavalcanti', 'valentina.c@email.com',     'senha123', 'Salvador',        'Feminino',  '1999-08-26'),
('Bruno',     'Lopes',      'bruno.lopes@email.com',     'senha123', 'Sao Paulo',       'Masculino', '1998-02-14'),
('Gabriela',  'Mendes',     'gabriela.mendes@email.com', 'senha123', 'Porto Alegre',    'Feminino',  '2000-06-09'),
('Andre',     'Freitas',    'andre.freitas@email.com',   'senha123', 'Curitiba',        'Masculino', '1996-03-22'),
('Patricia',  'Cardoso',    'patricia.cardoso@email.com','senha123', 'Rio de Janeiro',  'Feminino',  '1994-10-17'),
('Henrique',  'Pinto',      'henrique.pinto@email.com',  'senha123', 'Florianopolis',   'Masculino', '2000-09-01'),
('Renata',    'Teixeira',   'renata.teixeira@email.com', 'senha123', 'Brasilia',        'Feminino',  '1997-07-28'),
('Alexandre', 'Azevedo',    'alexandre.a@email.com',     'senha123', 'Sao Paulo',       'Masculino', '1993-05-11'),
('Leticia',   'Monteiro',   'leticia.monteiro@email.com','senha123', 'Maceio',          'Feminino',  '1998-01-06'),
('Gustavo',   'Xavier',     'gustavo.xavier@email.com',  'senha123', 'Belo Horizonte',  'Masculino', '1995-11-24'),
('Bruna',     'Figueiredo', 'bruna.figueiredo@email.com','senha123', 'Fortaleza',       'Feminino',  '2001-07-13'),
('Rodrigo',   'Nascimento', 'rodrigo.n@email.com',       'senha123', 'Recife',          'Masculino', '1994-04-29'),
('Mariana',   'Vieira',     'mariana.vieira@email.com',  'senha123', 'Goiania',         'Feminino',  '1999-02-16'),
('Leonardo',  'Campos',     'leonardo.campos@email.com', 'senha123', 'Porto Alegre',    'Masculino', '1996-08-05');

INSERT INTO perfil (descricao, preferencias, objetivos, tipo_perfil, altura, id_usuario) VALUES
('Adoro musica e viagens.', 'Alguem animado', 'Amizade sincera', 'amizade', 1.78, 1),
('Apaixonada por leitura e cinema.', 'Alguem intelectual', 'Relacionamento serio', 'relacionamento', 1.63, 2),
('Curto esportes e natureza.', 'Alguem ativo', 'Relacionamento serio', 'relacionamento', 1.82, 3),
('Amo cozinhar e receber amigos.', 'Alguem carinhoso', 'Amizade', 'amizade', 1.60, 4),
('Trabalho com tecnologia e adoro games.', 'Alguem parceiro', 'Relacionamento serio', 'relacionamento', 1.75, 5),
('Viajante nato, ja fui a 15 paises.', 'Alguem aventureiro', 'Amizade', 'amizade', 1.68, 6),
('Fotografo amador nas horas vagas.', 'Alguem criativo', 'Casual', 'casual', 1.80, 7),
('Ama danca e artes cenicas.', 'Alguem expressivo', 'Relacionamento serio', 'relacionamento', 1.65, 8),
('Entusiasta de astronomia.', 'Alguem curioso', 'Amizade', 'amizade', 1.77, 9),
('Veterinaria e amante de animais.', 'Alguem gentil', 'Relacionamento serio', 'relacionamento', 1.62, 10),
('Fanatico por futebol e churrasco.', 'Alguem descontraido', 'Casual', 'casual', 1.79, 11),
('Meditacao e yoga sao minha vida.', 'Alguem tranquilo', 'Relacionamento serio', 'relacionamento', 1.61, 12),
('Musico e compositor independente.', 'Alguem apreciador de musica', 'Casual', 'casual', 1.83, 13),
('Psicologia e comportamento humano.', 'Alguem empático', 'Relacionamento serio', 'relacionamento', 1.64, 14),
('Empreendedor e apaixonado por inovacao.', 'Alguem ambicioso', 'Relacionamento serio', 'relacionamento', 1.76, 15),
('Nutricionista focada em bem-estar.', 'Alguem saudavel', 'Amizade', 'amizade', 1.66, 16),
('Desenvolvedor e gamer nas horas vagas.', 'Alguem parceiro', 'Casual', 'casual', 1.81, 17),
('Educadora e apaixonada por criancas.', 'Alguem responsavel', 'Relacionamento serio', 'relacionamento', 1.59, 18),
('Surfista e amante do mar.', 'Alguem livre', 'Casual', 'casual', 1.84, 19),
('Chef de cozinha e exploradora de sabores.', 'Alguem apreciador', 'Relacionamento serio', 'relacionamento', 1.67, 20),
('Ciclista e defensor do meio ambiente.', 'Alguem consciente', 'Amizade', 'amizade', 1.74, 21),
('Escritora e leitora voraz.', 'Alguem inteligente', 'Relacionamento serio', 'relacionamento', 1.62, 22),
('Personal trainer e nutricionista.', 'Alguem saudavel', 'Casual', 'casual', 1.85, 23),
('Arquiteta e apaixonada por design.', 'Alguem criativo', 'Relacionamento serio', 'relacionamento', 1.69, 24),
('Advogado e amante de debates.', 'Alguem intelectual', 'Amizade', 'amizade', 1.79, 25),
('Medica e voluntaria em ONG.', 'Alguem solidario', 'Relacionamento serio', 'relacionamento', 1.63, 26),
('Engenheiro e piloto de drone.', 'Alguem tecnologico', 'Casual', 'casual', 1.87, 27),
('Professora universitaria e pesquisadora.', 'Alguem intelectual', 'Relacionamento serio', 'relacionamento', 1.65, 28),
('Empresario e investidor.', 'Alguem ambicioso', 'Casual', 'casual', 1.76, 29),
('Artesã e empreendedora criativa.', 'Alguem criativo', 'Amizade', 'amizade', 1.60, 30),
('Jogador profissional de xadrez.', 'Alguem estrategico', 'Amizade', 'amizade', 1.73, 31),
('Bailarina classica e professora de danca.', 'Alguem gracioso', 'Relacionamento serio', 'relacionamento', 1.61, 32),
('Bombeiro e praticante de mergulho.', 'Alguem corajoso', 'Casual', 'casual', 1.88, 33),
('Veterinaria especializada em fauna silvestre.', 'Alguem dedicado', 'Relacionamento serio', 'relacionamento', 1.64, 34),
('Astronomo amador e professor de fisica.', 'Alguem curioso', 'Amizade', 'amizade', 1.80, 35);

INSERT INTO foto (url_foto, data_upload, id_usuario) VALUES
('https://lovvi.app/fotos/usuario1_1.jpg',  '2025-01-10', 1),
('https://lovvi.app/fotos/usuario2_1.jpg',  '2025-01-12', 2),
('https://lovvi.app/fotos/usuario3_1.jpg',  '2025-01-15', 3),
('https://lovvi.app/fotos/usuario4_1.jpg',  '2025-01-18', 4),
('https://lovvi.app/fotos/usuario5_1.jpg',  '2025-01-20', 5),
('https://lovvi.app/fotos/usuario6_1.jpg',  '2025-01-22', 6),
('https://lovvi.app/fotos/usuario7_1.jpg',  '2025-01-25', 7),
('https://lovvi.app/fotos/usuario8_1.jpg',  '2025-01-28', 8),
('https://lovvi.app/fotos/usuario9_1.jpg',  '2025-02-01', 9),
('https://lovvi.app/fotos/usuario10_1.jpg', '2025-02-03', 10),
('https://lovvi.app/fotos/usuario11_1.jpg', '2025-02-05', 11),
('https://lovvi.app/fotos/usuario12_1.jpg', '2025-02-07', 12),
('https://lovvi.app/fotos/usuario13_1.jpg', '2025-02-10', 13),
('https://lovvi.app/fotos/usuario14_1.jpg', '2025-02-12', 14),
('https://lovvi.app/fotos/usuario15_1.jpg', '2025-02-14', 15),
('https://lovvi.app/fotos/usuario16_1.jpg', '2025-02-16', 16),
('https://lovvi.app/fotos/usuario17_1.jpg', '2025-02-18', 17),
('https://lovvi.app/fotos/usuario18_1.jpg', '2025-02-20', 18),
('https://lovvi.app/fotos/usuario19_1.jpg', '2025-02-22', 19),
('https://lovvi.app/fotos/usuario20_1.jpg', '2025-02-24', 20),
('https://lovvi.app/fotos/usuario21_1.jpg', '2025-02-26', 21),
('https://lovvi.app/fotos/usuario22_1.jpg', '2025-03-01', 22),
('https://lovvi.app/fotos/usuario23_1.jpg', '2025-03-03', 23),
('https://lovvi.app/fotos/usuario24_1.jpg', '2025-03-05', 24),
('https://lovvi.app/fotos/usuario25_1.jpg', '2025-03-07', 25),
('https://lovvi.app/fotos/usuario26_1.jpg', '2025-03-09', 26),
('https://lovvi.app/fotos/usuario27_1.jpg', '2025-03-11', 27),
('https://lovvi.app/fotos/usuario28_1.jpg', '2025-03-13', 28),
('https://lovvi.app/fotos/usuario29_1.jpg', '2025-03-15', 29),
('https://lovvi.app/fotos/usuario30_1.jpg', '2025-03-17', 30),
('https://lovvi.app/fotos/usuario31_1.jpg', '2025-03-19', 31),
('https://lovvi.app/fotos/usuario32_1.jpg', '2025-03-21', 32),
('https://lovvi.app/fotos/usuario33_1.jpg', '2025-03-23', 33),
('https://lovvi.app/fotos/usuario34_1.jpg', '2025-03-25', 34),
('https://lovvi.app/fotos/usuario35_1.jpg', '2025-03-27', 35);

INSERT INTO interesse (nome_interesse, categoria) VALUES
('Musica',            'Arte'),
('Cinema',            'Arte'),
('Fotografia',        'Arte'),
('Leitura',           'Cultura'),
('Viagens',           'Lazer'),
('Culinaria',         'Lazer'),
('Futebol',           'Esporte'),
('Natacao',           'Esporte'),
('Ciclismo',          'Esporte'),
('Yoga',              'Bem-estar'),
('Meditacao',         'Bem-estar'),
('Academia',          'Bem-estar'),
('Tecnologia',        'Profissional'),
('Empreendedorismo',  'Profissional'),
('Jogos',             'Entretenimento'),
('Series',            'Entretenimento'),
('Animals',           'Hobbie'),
('Jardinagem',        'Hobbie'),
('Teatro',            'Arte'),
('Danca',             'Arte'),
('Surf',              'Esporte'),
('Escalada',          'Esporte'),
('Astronomia',        'Ciencia'),
('Biologia marinha',  'Ciencia'),
('Voluntariado',      'Social'),
('Politica',          'Social'),
('Moda',              'Cultura'),
('Gastronomia',       'Lazer'),
('Podcast',           'Entretenimento'),
('Pintura',           'Arte');

INSERT INTO tem (id_usuario, id_interesse) VALUES
(1, 1), (1, 4), (2, 2), (2, 4), (3, 7),
(3, 9), (4, 6), (4, 20), (5, 13), (5, 15),
(6, 5), (6, 3), (7, 3), (7, 1), (8, 20),
(8, 19), (9, 23), (9, 4), (10, 17), (10, 10),
(11, 7), (11, 6), (12, 10), (12, 11), (13, 1),
(13, 19), (14, 4), (15, 14), (16, 12), (17, 13),
(17, 15), (18, 25), (19, 21), (20, 6), (21, 9);

INSERT INTO lovvi_match (id_usuario_1, id_usuario_2, data_match, compatibilidade, status_match) VALUES
(1,  2,  '2025-02-14', 87.50, 'aceito'),
(3,  4,  '2025-02-15', 72.30, 'aceito'),
(5,  6,  '2025-02-16', 65.80, 'pendente'),
(7,  8,  '2025-02-17', 91.20, 'aceito'),
(9,  10, '2025-02-18', 55.00, 'recusado'),
(11, 12, '2025-02-19', 78.90, 'aceito'),
(13, 14, '2025-02-20', 83.40, 'pendente'),
(15, 16, '2025-02-21', 69.70, 'aceito'),
(17, 18, '2025-02-22', 94.10, 'aceito'),
(19, 20, '2025-02-23', 61.50, 'recusado'),
(21, 22, '2025-02-24', 75.20, 'aceito'),
(23, 24, '2025-02-25', 88.60, 'pendente'),
(25, 26, '2025-02-26', 52.30, 'recusado'),
(27, 28, '2025-02-27', 96.80, 'aceito'),
(29, 30, '2025-02-28', 70.10, 'aceito'),
(1,  6,  '2025-03-01', 60.00, 'pendente'),
(2,  11, '2025-03-02', 82.70, 'aceito'),
(3,  14, '2025-03-03', 47.90, 'recusado'),
(4,  9,  '2025-03-04', 79.30, 'aceito'),
(5,  12, '2025-03-05', 85.50, 'aceito'),
(7,  16, '2025-03-06', 66.20, 'pendente'),
(8,  13, '2025-03-07', 73.80, 'aceito'),
(10, 15, '2025-03-08', 58.40, 'recusado'),
(11, 20, '2025-03-09', 90.00, 'aceito'),
(6,  19, '2025-03-10', 77.60, 'pendente'),
(18, 23, '2025-03-11', 84.30, 'aceito'),
(22, 27, '2025-03-12', 63.70, 'recusado'),
(24, 31, '2025-03-13', 89.90, 'aceito'),
(26, 33, '2025-03-14', 71.40, 'pendente'),
(28, 35, '2025-03-15', 95.20, 'aceito');

INSERT INTO usuario_amizade (id_usuario, tipo_role_preferido) VALUES
(1,  'amigo proximo'),
(2,  'colega de atividades'),
(3,  'parceiro de esportes'),
(4,  'amiga de conversas'),
(5,  'colega gamer'),
(6,  'companheira de viagem'),
(7,  'parceiro criativo'),
(8,  'amiga de artes'),
(9,  'parceiro de estudos'),
(10, 'amiga de animais'),
(11, 'parceiro de futebol'),
(12, 'amiga de bem-estar'),
(13, 'parceiro musical'),
(14, 'amiga de debates'),
(15, 'parceiro de negocios'),
(16, 'amiga de saude'),
(17, 'parceiro gamer'),
(18, 'amiga de educacao'),
(19, 'parceiro de surf'),
(20, 'amiga culinaria'),
(21, 'parceiro de ciclismo'),
(22, 'amiga literaria'),
(23, 'parceiro de treinos'),
(24, 'amiga de design'),
(25, 'parceiro de debates'),
(26, 'amiga solidaria'),
(27, 'parceiro de tecnologia'),
(28, 'amiga academica'),
(29, 'parceiro de investimentos'),
(30, 'amiga artesanal');

INSERT INTO usuario_relacionamento_serio (id_usuario, distancia_maxima, tipo_relacionamento, pretende_ter_filhos) VALUES
(6,  50,  'monogamico',   1),
(7,  30,  'monogamico',   0),
(8,  40,  'monogamico',   1),
(9,  60,  'longa distancia', 0),
(10, 25,  'monogamico',   1),
(11, 35,  'monogamico',   0),
(12, 45,  'monogamico',   1),
(13, 20,  'aberto',       0),
(14, 55,  'monogamico',   1),
(15, 30,  'monogamico',   0),
(16, 40,  'monogamico',   1),
(17, 50,  'aberto',       0),
(18, 15,  'monogamico',   1),
(19, 70,  'longa distancia', 0),
(20, 25,  'monogamico',   1),
(21, 35,  'monogamico',   0),
(22, 45,  'monogamico',   1),
(23, 60,  'aberto',       0),
(24, 20,  'monogamico',   1),
(25, 30,  'monogamico',   1),
(26, 10,  'monogamico',   1),
(27, 80,  'longa distancia', 0),
(28, 20,  'monogamico',   1),
(29, 40,  'aberto',       0),
(30, 30,  'monogamico',   1),
(31, 50,  'monogamico',   0),
(32, 25,  'monogamico',   1),
(33, 60,  'longa distancia', 0),
(34, 15,  'monogamico',   1),
(35, 35,  'monogamico',   0);

INSERT INTO teste (nome_teste, descricao) VALUES
('Compatibilidade Geral',    'Teste para avaliar a compatibilidade geral entre usuarios'),
('Estilo de Vida',           'Avalia preferencias de rotina e habitos diarios'),
('Valores e Crencas',        'Avalia principios pessoais e visao de mundo'),
('Comunicacao',              'Testa o estilo de comunicacao e resolucao de conflitos'),
('Relacionamento Ideal',     'Descobre o que o usuario busca em um parceiro'),
('Humor e Personalidade',    'Avalia senso de humor e tracos de personalidade'),
('Ambicoes e Metas',         'Verifica objetivos de vida e ambicoes profissionais'),
('Familia e Filhos',         'Avalia planos familiares e desejo de ter filhos'),
('Interesses Culturais',     'Mede afinidade com cultura, arte e entretenimento'),
('Saude e Bem-estar',        'Avalia habitos de saude, exercicio e alimentacao'),
('Comunidade e Causas',      'Mede engajamento social, voluntariado e causas humanas'),
('Planejamento Financeiro',  'Avalia organizacao financeira e visao de futuro'),
('Rotina de Trabalho',       'Avalia equilibrio entre carreira e vida pessoal'),
('Preferencias de Viagem',   'Compara estilos e destinos de viagem preferidos'),
('Tecnologia e Inovacao',    'Mede afinidade com tecnologia e novas tendencias'),
('Convivio Familiar',        'Avalia relacao com familia e convivencia diaria'),
('Saude Emocional',          'Mede capacidade de autocuidado e maturidade emocional'),
('Objetivos Academicos',     'Compara objetivos de estudo e desenvolvimento'),
('Consumo Cultural',         'Avalia habitos de leitura, cinema, teatro e musica'),
('Espiritualidade',          'Mede visao espiritual e valores transcendentais'),
('Lideranca e Colaboracao',  'Avalia postura em grupo, lideranca e trabalho colaborativo'),
('Estilo de Comunicacao',    'Mapeia clareza, escuta e assertividade na comunicacao'),
('Gestao de Conflitos',      'Avalia como a pessoa reage e resolve divergencias'),
('Adaptabilidade',           'Mede flexibilidade diante de mudancas e imprevistos'),
('Hobbies Criativos',        'Compara interesse por artes, escrita e criacao'),
('Rotina Esportiva',         'Avalia frequencia e motivacao para atividade fisica'),
('Proposito de Vida',        'Mede alinhamento entre valores, metas e significado pessoal'),
('Empatia e Escuta',         'Avalia abertura para compreender o outro no dia a dia'),
('Vinculos e Convivencia',   'Mede consistencia de convivio e qualidade dos vinculos sociais'),
('Confianca e Transparencia','Mede nivel de honestidade e construcao de confianca');

INSERT INTO pergunta (texto_pergunta, categoria, id_teste) VALUES
('Qual e sua atividade favorita no fim de semana?', 'Lazer', 1),
('Como voce prefere passar o tempo livre?',         'Lazer', 1),
('O que mais te atrai em uma pessoa?',              'Relacionamento', 1),
('Voce prefere sair ou ficar em casa?',             'Lazer', 2),
('Com que frequencia voce pratica exercicios?',     'Saude', 2),
('Qual e seu horario mais produtivo?',              'Rotina', 2),
('O que nao pode faltar em sua vida?',              'Valores', 3),
('Voce se considera religioso?',                    'Crencas', 3),
('Qual causa social mais te motiva?',               'Social', 3),
('Como voce reage quando esta com raiva?',          'Comportamento', 4),
('Voce prefere resolver conflitos como?',           'Comunicacao', 4),
('Com que frequencia voce demonstra afeto?',        'Relacionamento', 4),
('O que procura em um relacionamento?',             'Relacionamento', 5),
('Quantos filhos voce deseja ter?',                 'Familia', 5),
('Onde gostaria de morar no futuro?',               'Vida', 5),
('Qual tipo de humor te diverte mais?',             'Personalidade', 6),
('Como seus amigos te descrevem?',                  'Personalidade', 6),
('Voce e mais introvertido ou extrovertido?',       'Personalidade', 6),
('Quais sao seus objetivos profissionais?',         'Carreira', 7),
('Em 5 anos, onde voce quer estar?',                'Futuro', 7),
('Qua importancia tem dinheiro para voce?',         'Valores', 7),
('Voce quer ter filhos?',                           'Familia', 8),
('Qual seria a idade ideal para casar?',            'Familia', 8),
('Seus pais influenciam suas decisoes?',            'Familia', 8),
('Qual genero musical voce mais gosta?',            'Cultura', 9),
('Com que frequencia voce vai ao cinema?',          'Cultura', 9),
('Voce prefere livros ou series?',                  'Entretenimento', 9),
('Com que frequencia voce se exercita?',            'Saude', 10),
('O que voce come no cafe da manha?',               'Saude', 10),
('Voce dorme quantas horas por noite?',             'Saude', 10);

INSERT INTO opcao_resposta (texto_opcao, id_pergunta) VALUES
-- Pergunta 1
('Praticar esportes', 1), ('Ler livros', 1), ('Assistir series', 1), ('Sair com amigos', 1),
-- Pergunta 2
('Viajar', 2), ('Descansar em casa', 2), ('Fazer cursos', 2), ('Praticar hobbies', 2),
-- Pergunta 3
('Senso de humor', 3), ('Inteligencia', 3), ('Aparencia', 3), ('Empatia', 3),
-- Pergunta 4
('Sair', 4), ('Ficar em casa', 4), ('Depende do dia', 4), ('Meio a meio', 4),
-- Pergunta 5
('Todo dia', 5), ('3 a 4 vezes por semana', 5), ('Raramente', 5), ('Nao pratico', 5),
-- Pergunta 6
('Manha', 6), ('Tarde', 6), ('Noite', 6), ('Variavel', 6),
-- Pergunta 7
('Saude', 7), ('Familia', 7), ('Liberdade', 7), ('Dinheiro', 7),
-- Pergunta 8
('Sim, muito', 8), ('Um pouco', 8), ('Nao muito', 8), ('Nao me considero', 8),
-- Pergunta 9
('Meio ambiente', 9), ('Educacao', 9), ('Saude', 9), ('Direitos humanos', 9),
-- Pergunta 10
('Fico quieto', 10), ('Converso', 10), ('Me afasto', 10), ('Expresso diretamente', 10),
-- Pergunta 11
('Conversando', 11), ('Com tempo', 11), ('Buscando mediacao', 11), ('Evitando', 11),
-- Pergunta 12
('Sempre', 12), ('As vezes', 12), ('Raramente', 12), ('Depende', 12),
-- Pergunta 13
('Companheirismo', 13), ('Paixao', 13), ('Estabilidade', 13), ('Crescimento mutuo', 13),
-- Pergunta 14
('Nenhum', 14), ('1 filho', 14), ('2 filhos', 14), ('Mais de 2', 14),
-- Pergunta 15
('Grande cidade', 15), ('Interior', 15), ('Litoral', 15), ('Exterior', 15),
-- Pergunta 16
('Humor ironico', 16), ('Humor fisico', 16), ('Humor inteligente', 16), ('Tudo me diverte', 16),
-- Pergunta 17
('Extrovertido', 17), ('Introvertido', 17), ('Reservado', 17), ('Equilibrado', 17),
-- Pergunta 18
('Totalmente extrovertido', 18), ('Mais extrovertido', 18), ('Mais introvertido', 18), ('Totalmente introvertido', 18),
-- Pergunta 19
('Ser empreendedor', 19), ('Trabalhar numa grande empresa', 19), ('Ser servidor publico', 19), ('Seguir minha paixao', 19),
-- Pergunta 20
('Bem estabelecido profissionalmente', 20), ('Viajando pelo mundo', 20), ('Com familia', 20), ('Estudando mais', 20),
-- Pergunta 21
('Muito importante', 21), ('Importante mas nao essencial', 21), ('Pouco importante', 21), ('Nao me importa', 21),
-- Pergunta 22
('Sim, definitivamente', 22), ('Talvez', 22), ('Nao tenho certeza', 22), ('Nao quero', 22),
-- Pergunta 23
('Antes dos 25', 23), ('Entre 25 e 30', 23), ('Depois dos 30', 23), ('Nao penso em casar', 23),
-- Pergunta 24
('Sim, muito', 24), ('Considero a opiniao', 24), ('Pouco', 24), ('Nao interferem', 24),
-- Pergunta 25
('Rock', 25), ('Pop', 25), ('Sertanejo', 25), ('Variado', 25),
-- Pergunta 26
('Toda semana', 26), ('Uma vez por mes', 26), ('Raramente', 26), ('Nunca', 26),
-- Pergunta 27
('Livros', 27), ('Series', 27), ('Ambos igualmente', 27), ('Nenhum', 27),
-- Pergunta 28
('Todo dia', 28), ('3x por semana', 28), ('Raramente', 28), ('Nunca', 28),
-- Pergunta 29
('Frutas e granola', 29), ('Pao com manteiga', 29), ('Ovos', 29), ('Nao como', 29),
-- Pergunta 30
('Menos de 6h', 30), ('6 a 7 horas', 30), ('8 horas', 30), ('Mais de 8h', 30);

INSERT INTO resposta (id_usuario, id_pergunta, id_opcao) VALUES
(1,  1,  2),  -- Joao respondeu "Ler livros" na pergunta 1
(2,  1,  4),  -- Beatriz respondeu "Sair com amigos"
(3,  2,  1),  -- Carlos respondeu "Viajar"
(4,  3,  4),  -- Ana respondeu "Empatia"
(5,  4,  14), -- Pedro respondeu "Depende do dia"
(6,  5,  17), -- Fernanda respondeu "Raramente"
(7,  6,  21), -- Lucas respondeu "Manha"
(8,  7,  26), -- Marina respondeu "Familia"
(9,  8,  32), -- Gabriel respondeu "Um pouco"
(10, 9,  35), -- Camila respondeu "Educacao"
(11, 10, 37), -- Rafael respondeu "Converso"
(12, 11, 42), -- Julia respondeu "Com tempo"
(13, 12, 46), -- Diego respondeu "As vezes"
(14, 13, 50), -- Larissa respondeu "Estabilidade"
(15, 14, 53), -- Thiago respondeu "2 filhos"
(16, 15, 57), -- Natalia respondeu "Litoral"
(17, 16, 60), -- Felipe respondeu "Humor inteligente"
(18, 17, 64), -- Isabella respondeu "Equilibrado"
(19, 18, 67), -- Matheus respondeu "Mais extrovertido"
(20, 19, 71), -- Sofia respondeu "Seguir minha paixao"
(21, 20, 73), -- Eduardo respondeu "Com familia"
(22, 21, 77), -- Valentina respondeu "Importante mas nao essencial"
(23, 22, 81), -- Bruno respondeu "Sim, definitivamente"
(24, 23, 86), -- Gabriela respondeu "Entre 25 e 30"
(25, 24, 90), -- Andre respondeu "Considero a opiniao"
(26, 25, 92), -- Patricia respondeu "Pop"
(27, 26, 97), -- Henrique respondeu "Raramente"
(28, 27, 99), -- Renata respondeu "Ambos igualmente"
(29, 28, 101),-- Alexandre respondeu "Todo dia"
(30, 29, 106);-- Leticia respondeu "Ovos"
