# Lovvi

Aplicacao web de relacionamento integrada a um banco de dados MySQL, feita em
Java com Spring Boot, Thymeleaf, HTML, CSS e JavaScript.

Este README explica, de forma simples e direta, como executar o projeto.

## Requisitos

- Java 21 instalado
- Maven instalado
- MySQL 8 instalado e em execucao
- Navegador web

## 1. Baixar ou abrir o projeto

Entre na pasta do projeto:

```powershell
cd Lovvi\lovvi-app
```

## 2. Preparar o banco de dados

No MySQL Workbench, DBeaver ou terminal MySQL, execute os scripts abaixo nesta
ordem:

```text
src/main/resources/sql/criar_banco.sql
src/main/resources/sql/criar_tabelas.sql
src/main/resources/sql/popular_dados.sql
src/main/resources/sql/consultas_views_indices.sql
src/main/resources/sql/funcoes_procedimentos_triggers.sql
```

Pelo terminal, a partir da pasta `Lovvi\lovvi-app`, tambem e possivel executar:

```powershell
mysql -u root -p < src/main/resources/sql/criar_banco.sql
mysql -u root -p lovvi_db < src/main/resources/sql/criar_tabelas.sql
mysql -u root -p lovvi_db < src/main/resources/sql/popular_dados.sql
mysql -u root -p lovvi_db < src/main/resources/sql/consultas_views_indices.sql
mysql -u root -p lovvi_db < src/main/resources/sql/funcoes_procedimentos_triggers.sql
```

O arquivo `src/main/resources/sql/ordem_execucao.md` tambem mostra a ordem dos
scripts.

## 3. Configurar usuario e senha do MySQL

A aplicacao usa por padrao:

```text
usuario: root
senha: root
banco: lovvi_db
porta: 3306
```

Se a senha do seu MySQL for diferente, configure antes de iniciar:

```powershell
$env:DB_USER="root"
$env:DB_PASSWORD="sua_senha_do_mysql"
```

Tambem e possivel alterar diretamente o arquivo:

```text
src/main/resources/application.properties
```

## 4. Executar a aplicacao

Na pasta `Lovvi\lovvi-app`, rode:

```powershell
mvn spring-boot:run
```

Quando o Spring Boot terminar de iniciar, abra no navegador:

```text
http://localhost:8080
```

## Paginas principais

```text
http://localhost:8080/              Pagina inicial
http://localhost:8080/cadastro      Cadastro de usuario
http://localhost:8080/login         Login
http://localhost:8080/perfil/1      Tela de matches do usuario 1
http://localhost:8080/interface     Interface do banco de dados
```

A interface do banco tambem possui paginas separadas:

```text
http://localhost:8080/interface/crud
http://localhost:8080/interface/consultas
http://localhost:8080/interface/rotinas
http://localhost:8080/interface/dashboard
http://localhost:8080/interface/tudo
```

## Atualizar rotinas sem apagar dados

Depois que existirem usuarios reais cadastrados, nao execute novamente
`criar_tabelas.sql` nem `popular_dados.sql`, pois esses scripts recriam ou
repovoam a base.

Para atualizar apenas views, indices, funcoes, procedures e triggers, use:

```powershell
mysql -u root -p lovvi_db < src/main/resources/sql/manutencao_rotinas.sql
```

## Erro comum

Se aparecer erro 500 ao abrir o site, confira:

- se o MySQL esta aberto;
- se o banco `lovvi_db` foi criado;
- se os scripts SQL foram executados na ordem correta;
- se usuario e senha do MySQL estao corretos em `application.properties` ou nas variaveis `DB_USER` e `DB_PASSWORD`.
