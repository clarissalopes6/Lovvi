package com.lovvi.controller;

import com.lovvi.dao.DatabaseViewerDAO;
import com.lovvi.dao.EtapaFinalDAO;
import com.lovvi.dao.InteresseDAO;
import com.lovvi.dao.LovviMatchDAO;
import com.lovvi.dao.TesteDAO;
import com.lovvi.dao.UsuarioDAO;
import com.lovvi.dto.CompatibilidadeFunctionRequest;
import com.lovvi.dto.CrudResult;
import com.lovvi.dto.DashboardData;
import com.lovvi.dto.DatabaseOverview;
import com.lovvi.dto.FunctionResult;
import com.lovvi.dto.IdadeFunctionRequest;
import com.lovvi.dto.InteresseCrudRequest;
import com.lovvi.dto.InteresseCrudRow;
import com.lovvi.dto.LovviMatchCrudRequest;
import com.lovvi.dto.LovviMatchCrudRow;
import com.lovvi.dto.MatchStatusRequest;
import com.lovvi.dto.RelatorioInfo;
import com.lovvi.dto.TableDetails;
import com.lovvi.dto.TesteCrudRequest;
import com.lovvi.dto.TesteCrudRow;
import com.lovvi.dto.UsuarioCrudRequest;
import com.lovvi.dto.UsuarioCrudRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/interface")
public class DatabaseViewerApiController {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseViewerApiController.class);

    private final DatabaseViewerDAO databaseViewerDAO;
    private final UsuarioDAO usuarioDAO;
    private final TesteDAO testeDAO;
    private final InteresseDAO interesseDAO;
    private final LovviMatchDAO lovviMatchDAO;
    private final EtapaFinalDAO etapaFinalDAO;

    public DatabaseViewerApiController(
            DatabaseViewerDAO databaseViewerDAO,
            UsuarioDAO usuarioDAO,
            TesteDAO testeDAO,
            InteresseDAO interesseDAO,
            LovviMatchDAO lovviMatchDAO,
            EtapaFinalDAO etapaFinalDAO
    ) {
        this.databaseViewerDAO = databaseViewerDAO;
        this.usuarioDAO = usuarioDAO;
        this.testeDAO = testeDAO;
        this.interesseDAO = interesseDAO;
        this.lovviMatchDAO = lovviMatchDAO;
        this.etapaFinalDAO = etapaFinalDAO;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int safeLimit(int limit, int max) {
        return Math.max(1, Math.min(limit, max));
    }

    private boolean statusInvalido(String status) {
        if (isBlank(status)) {
            return true;
        }
        String value = status.trim().toLowerCase();
        return !value.equals("pendente") && !value.equals("aceito") && !value.equals("recusado");
    }

    @GetMapping("/overview")
    public ResponseEntity<DatabaseOverview> getOverview() {
        try {
            return ResponseEntity.ok(databaseViewerDAO.obterVisaoGeral());
        } catch (SQLException e) {
            logger.error("Erro ao montar visao geral do banco", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tables/{tableName}")
    public ResponseEntity<TableDetails> getTableDetails(
            @PathVariable("tableName") String tableName,
            @RequestParam(name = "limit", defaultValue = "50") int limit
    ) {
        try {
            TableDetails details = databaseViewerDAO.obterDetalhesTabela(tableName, safeLimit(limit, 200));
            if (details == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(details);
        } catch (SQLException e) {
            logger.error("Erro ao montar detalhes da tabela {}", tableName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioCrudRow>> listUsuarios(
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        try {
            return ResponseEntity.ok(usuarioDAO.listarCrud(safeLimit(limit, 300)));
        } catch (SQLException e) {
            logger.error("Erro ao listar usuarios", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/usuarios")
    public ResponseEntity<CrudResult> createUsuario(@RequestBody UsuarioCrudRequest request) {
        if (usuarioInvalido(request)) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Dados obrigatorios do usuario ausentes.", null));
        }

        try {
            int createdId = usuarioDAO.inserirCrud(request);
            return ResponseEntity.ok(new CrudResult(true, "Usuario inserido com sucesso.", createdId));
        } catch (SQLException e) {
            logger.error("Erro ao inserir usuario", e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao inserir usuario.", null));
        }
    }

    @PutMapping("/usuarios/{idUsuario}")
    public ResponseEntity<CrudResult> updateUsuario(
            @PathVariable("idUsuario") int idUsuario,
            @RequestBody UsuarioCrudRequest request
    ) {
        if (usuarioInvalido(request)) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Dados obrigatorios do usuario ausentes.", null));
        }

        try {
            boolean updated = usuarioDAO.atualizarCrud(idUsuario, request);
            if (!updated) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Usuario nao encontrado.", null));
            }

            return ResponseEntity.ok(new CrudResult(true, "Usuario atualizado com sucesso.", idUsuario));
        } catch (SQLException e) {
            logger.error("Erro ao atualizar usuario {}", idUsuario, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao atualizar usuario.", null));
        }
    }

    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<CrudResult> deleteUsuario(@PathVariable("idUsuario") int idUsuario) {
        try {
            boolean deleted = usuarioDAO.excluirCrud(idUsuario);
            if (!deleted) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Usuario nao encontrado.", null));
            }

            return ResponseEntity.ok(new CrudResult(true, "Usuario removido com sucesso.", idUsuario));
        } catch (SQLException e) {
            logger.error("Erro ao remover usuario {}", idUsuario, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao remover usuario.", null));
        }
    }

    private boolean usuarioInvalido(UsuarioCrudRequest request) {
        return request == null
                || isBlank(request.nome())
                || isBlank(request.sobrenome())
                || isBlank(request.email())
                || isBlank(request.senha())
                || isBlank(request.cidade())
                || isBlank(request.genero())
                || request.dtNascimento() == null;
    }

    @GetMapping("/testes")
    public ResponseEntity<List<TesteCrudRow>> listTestes(
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        try {
            return ResponseEntity.ok(testeDAO.listar(safeLimit(limit, 300)));
        } catch (SQLException e) {
            logger.error("Erro ao listar testes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/testes")
    public ResponseEntity<CrudResult> createTeste(@RequestBody TesteCrudRequest request) {
        if (request == null || isBlank(request.nomeTeste())) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Nome do teste e obrigatorio.", null));
        }

        try {
            int createdId = testeDAO.inserir(request);
            return ResponseEntity.ok(new CrudResult(true, "Teste inserido com sucesso.", createdId));
        } catch (SQLException e) {
            logger.error("Erro ao inserir teste", e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao inserir teste.", null));
        }
    }

    @PutMapping("/testes/{idTeste}")
    public ResponseEntity<CrudResult> updateTeste(
            @PathVariable("idTeste") int idTeste,
            @RequestBody TesteCrudRequest request
    ) {
        if (request == null || isBlank(request.nomeTeste())) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Nome do teste e obrigatorio.", null));
        }

        try {
            boolean updated = testeDAO.atualizar(idTeste, request);
            if (!updated) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Teste nao encontrado.", null));
            }

            return ResponseEntity.ok(new CrudResult(true, "Teste atualizado com sucesso.", idTeste));
        } catch (SQLException e) {
            logger.error("Erro ao atualizar teste {}", idTeste, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao atualizar teste.", null));
        }
    }

    @DeleteMapping("/testes/{idTeste}")
    public ResponseEntity<CrudResult> deleteTeste(@PathVariable("idTeste") int idTeste) {
        try {
            boolean deleted = testeDAO.excluir(idTeste);
            if (!deleted) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Teste nao encontrado.", null));
            }

            return ResponseEntity.ok(new CrudResult(true, "Teste removido com sucesso.", idTeste));
        } catch (SQLException e) {
            logger.error("Erro ao remover teste {}", idTeste, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao remover teste.", null));
        }
    }

    @GetMapping("/interesses")
    public ResponseEntity<List<InteresseCrudRow>> listInteresses(
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        try {
            return ResponseEntity.ok(interesseDAO.listarCrud(safeLimit(limit, 300)));
        } catch (SQLException e) {
            logger.error("Erro ao listar interesses", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/interesses")
    public ResponseEntity<CrudResult> createInteresse(@RequestBody InteresseCrudRequest request) {
        if (request == null || isBlank(request.nomeInteresse()) || isBlank(request.categoria())) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Nome e categoria do interesse sao obrigatorios.", null));
        }

        try {
            int createdId = interesseDAO.inserir(request);
            return ResponseEntity.ok(new CrudResult(true, "Interesse inserido com sucesso.", createdId));
        } catch (SQLException e) {
            logger.error("Erro ao inserir interesse", e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao inserir interesse.", null));
        }
    }

    @PutMapping("/interesses/{idInteresse}")
    public ResponseEntity<CrudResult> updateInteresse(
            @PathVariable("idInteresse") int idInteresse,
            @RequestBody InteresseCrudRequest request
    ) {
        if (request == null || isBlank(request.nomeInteresse()) || isBlank(request.categoria())) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Nome e categoria do interesse sao obrigatorios.", null));
        }

        try {
            boolean updated = interesseDAO.atualizar(idInteresse, request);
            if (!updated) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Interesse nao encontrado.", null));
            }
            return ResponseEntity.ok(new CrudResult(true, "Interesse atualizado com sucesso.", idInteresse));
        } catch (SQLException e) {
            logger.error("Erro ao atualizar interesse {}", idInteresse, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao atualizar interesse.", null));
        }
    }

    @DeleteMapping("/interesses/{idInteresse}")
    public ResponseEntity<CrudResult> deleteInteresse(@PathVariable("idInteresse") int idInteresse) {
        try {
            boolean deleted = interesseDAO.excluir(idInteresse);
            if (!deleted) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Interesse nao encontrado.", null));
            }
            return ResponseEntity.ok(new CrudResult(true, "Interesse removido com sucesso.", idInteresse));
        } catch (SQLException e) {
            logger.error("Erro ao remover interesse {}", idInteresse, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao remover interesse.", null));
        }
    }

    @GetMapping("/matches")
    public ResponseEntity<List<LovviMatchCrudRow>> listMatches(
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        try {
            return ResponseEntity.ok(lovviMatchDAO.listar(safeLimit(limit, 300)));
        } catch (SQLException e) {
            logger.error("Erro ao listar matches", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/matches")
    public ResponseEntity<CrudResult> createMatch(@RequestBody LovviMatchCrudRequest request) {
        if (matchInvalido(request)) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Dados invalidos para o match.", null));
        }

        try {
            int createdId = lovviMatchDAO.inserir(request);
            return ResponseEntity.ok(new CrudResult(true, "Match inserido com sucesso.", createdId));
        } catch (SQLException e) {
            logger.error("Erro ao inserir match", e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao inserir match.", null));
        }
    }

    @PutMapping("/matches/{idMatch}")
    public ResponseEntity<CrudResult> updateMatch(
            @PathVariable("idMatch") int idMatch,
            @RequestBody LovviMatchCrudRequest request
    ) {
        if (matchInvalido(request)) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Dados invalidos para o match.", null));
        }

        try {
            boolean updated = lovviMatchDAO.atualizar(idMatch, request);
            if (!updated) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Match nao encontrado.", null));
            }
            return ResponseEntity.ok(new CrudResult(true, "Match atualizado com sucesso.", idMatch));
        } catch (SQLException e) {
            logger.error("Erro ao atualizar match {}", idMatch, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao atualizar match.", null));
        }
    }

    @DeleteMapping("/matches/{idMatch}")
    public ResponseEntity<CrudResult> deleteMatch(@PathVariable("idMatch") int idMatch) {
        try {
            boolean deleted = lovviMatchDAO.excluir(idMatch);
            if (!deleted) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Match nao encontrado.", null));
            }
            return ResponseEntity.ok(new CrudResult(true, "Match removido com sucesso.", idMatch));
        } catch (SQLException e) {
            logger.error("Erro ao remover match {}", idMatch, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao remover match.", null));
        }
    }

    private boolean matchInvalido(LovviMatchCrudRequest request) {
        return request == null
                || request.idUsuario1() <= 0
                || request.idUsuario2() <= 0
                || request.idUsuario1() == request.idUsuario2()
                || request.compatibilidade() == null
                || request.compatibilidade().doubleValue() < 0
                || request.compatibilidade().doubleValue() > 100
                || statusInvalido(request.statusMatch());
    }

    @GetMapping("/relatorios")
    public ResponseEntity<List<RelatorioInfo>> listRelatorios() {
        return ResponseEntity.ok(List.of(
                new RelatorioInfo("categorias-interesses", "Categorias de interesses", "Join com group by e having."),
                new RelatorioInfo("usuarios-por-cidade", "Usuarios por cidade", "Consulta com varios joins e filtro por cidade."),
                new RelatorioInfo("usuarios-sem-perfil", "Usuarios sem perfil", "Anti join com left join."),
                new RelatorioInfo("matches-acima-media", "Matches acima da media", "Consulta com subconsulta."),
                new RelatorioInfo("view-perfil-completo", "View perfil completo", "View com perfil, usuario e interesses."),
                new RelatorioInfo("view-usuarios-destaque", "View usuarios destaque", "View com subconsulta de media de compatibilidade.")
        ));
    }

    @GetMapping("/relatorios/{id}")
    public ResponseEntity<TableDetails> executarRelatorio(
            @PathVariable("id") String id,
            @RequestParam(name = "cidade", required = false) String cidade,
            @RequestParam(name = "tipoPerfil", required = false) String tipoPerfil,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "limit", defaultValue = "50") int limit
    ) {
        try {
            TableDetails details = etapaFinalDAO.executarRelatorio(
                    id,
                    cidade,
                    tipoPerfil,
                    categoria,
                    status,
                    safeLimit(limit, 200)
            );
            return details == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(details);
        } catch (SQLException e) {
            logger.error("Erro ao executar relatorio {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardData> getDashboard() {
        try {
            return ResponseEntity.ok(etapaFinalDAO.montarDashboard());
        } catch (SQLException e) {
            logger.error("Erro ao montar dashboard", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/funcoes/idade")
    public ResponseEntity<FunctionResult> calcularIdade(@RequestBody IdadeFunctionRequest request) {
        if (request == null || request.dtNascimento() == null) {
            return ResponseEntity.badRequest().body(new FunctionResult("fn_calcular_idade_usuario", null, "Data de nascimento obrigatoria."));
        }

        try {
            Integer idade = etapaFinalDAO.calcularIdade(request.dtNascimento());
            return ResponseEntity.ok(new FunctionResult("fn_calcular_idade_usuario", idade, "Funcao executada com sucesso."));
        } catch (SQLException e) {
            logger.error("Erro ao executar funcao de idade", e);
            return ResponseEntity.internalServerError().body(new FunctionResult("fn_calcular_idade_usuario", null, "Erro ao executar funcao."));
        }
    }

    @PostMapping("/funcoes/compatibilidade")
    public ResponseEntity<FunctionResult> calcularCompatibilidade(@RequestBody CompatibilidadeFunctionRequest request) {
        if (request == null || request.idUsuario1() <= 0 || request.idUsuario2() <= 0) {
            return ResponseEntity.badRequest().body(new FunctionResult("fn_calcular_compatibilidade", null, "IDs de usuarios obrigatorios."));
        }

        try {
            return ResponseEntity.ok(new FunctionResult(
                    "fn_calcular_compatibilidade",
                    etapaFinalDAO.calcularCompatibilidade(request.idUsuario1(), request.idUsuario2()),
                    "Funcao executada com sucesso."
            ));
        } catch (SQLException e) {
            logger.error("Erro ao executar funcao de compatibilidade", e);
            return ResponseEntity.internalServerError().body(new FunctionResult("fn_calcular_compatibilidade", null, "Erro ao executar funcao."));
        }
    }

    @PostMapping("/procedimentos/match-status")
    public ResponseEntity<CrudResult> atualizarStatusMatch(@RequestBody MatchStatusRequest request) {
        if (request == null || request.idMatch() <= 0 || statusInvalido(request.statusMatch())) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Informe um match e status valido.", null));
        }

        try {
            etapaFinalDAO.atualizarStatusMatch(request.idMatch(), request.statusMatch());
            return ResponseEntity.ok(new CrudResult(true, "Procedimento executado; verifique o log da trigger.", request.idMatch()));
        } catch (SQLException e) {
            logger.error("Erro ao executar procedimento de status", e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao executar procedimento.", null));
        }
    }

    @GetMapping("/logs/matches")
    public ResponseEntity<TableDetails> listarLogsMatch(
            @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        try {
            return ResponseEntity.ok(etapaFinalDAO.listarLogsMatch(safeLimit(limit, 100)));
        } catch (SQLException e) {
            logger.error("Erro ao listar logs de match", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
