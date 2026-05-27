# Ordem de execucao dos scripts SQL

## Primeira instalacao

Execute os arquivos nesta ordem no MySQL somente quando o banco ainda estiver
vazio ou quando voce quiser montar o projeto do zero:

1. `criar_banco.sql`
2. `criar_tabelas.sql`
3. `popular_dados.sql`
4. `consultas_views_indices.sql`
5. `funcoes_procedimentos_triggers.sql`

Depois inicie a aplicacao Java e acesse `/interface`.

## Manutencao sem perder dados

Depois que voce criar usuarios pela interface, nao rode novamente
`criar_tabelas.sql` nem `popular_dados.sql`.

Para atualizar apenas views, indices, funcoes, procedimentos e triggers, execute:

1. `manutencao_rotinas.sql`

Esse script nao apaga usuarios, perfis, interesses, matches ou respostas. Ele
so recria objetos de apoio e cria `log_match` se a tabela ainda nao existir.

Antes de rodar qualquer script, voce pode conferir se ja existem dados:

```sql
USE lovvi_db;
SHOW TABLES;
SELECT COUNT(*) FROM usuario;
```

Observacao: a aplicacao usa JDBC puro. As consultas, views, funcoes,
procedimentos e triggers sao chamados explicitamente pelos DAOs.
