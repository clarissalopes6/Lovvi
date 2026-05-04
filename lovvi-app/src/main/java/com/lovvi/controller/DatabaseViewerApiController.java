package com.lovvi.controller;

import com.lovvi.dao.DatabaseViewerDAO;
import com.lovvi.dao.TesteDAO;
import com.lovvi.dao.UsuarioDAO;
import com.lovvi.dto.CrudResult;
import com.lovvi.dto.DatabaseOverview;
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

    public DatabaseViewerApiController(DatabaseViewerDAO databaseViewerDAO, UsuarioDAO usuarioDAO, TesteDAO testeDAO) {
        this.databaseViewerDAO = databaseViewerDAO;
        this.usuarioDAO = usuarioDAO;
        this.testeDAO = testeDAO;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int safeLimit(int limit, int max) {
        return Math.max(1, Math.min(limit, max));
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
}
