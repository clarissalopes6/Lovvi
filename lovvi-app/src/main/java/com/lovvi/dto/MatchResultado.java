package com.lovvi.dto;

import java.util.List;

public record MatchResultado(
        int idUsuario,
        String nomeCompleto,
        String cidade,
        String tipoPerfil,
        double compatibilidade,
        List<String> interesses
) {
}
