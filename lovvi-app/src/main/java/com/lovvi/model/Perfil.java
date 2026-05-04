package com.lovvi.model;

import java.math.BigDecimal;

public record Perfil(
        int idPerfil,
        String descricao,
        String preferencias,
        String objetivos,
        String tipoPerfil,
        BigDecimal altura,
        int idUsuario
) {
}
