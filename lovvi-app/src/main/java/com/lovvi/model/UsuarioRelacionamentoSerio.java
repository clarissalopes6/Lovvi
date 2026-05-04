package com.lovvi.model;

public record UsuarioRelacionamentoSerio(
        int idUsuario,
        Integer distanciaMaxima,
        String tipoRelacionamento,
        boolean pretendeTerFilhos
) {
}
