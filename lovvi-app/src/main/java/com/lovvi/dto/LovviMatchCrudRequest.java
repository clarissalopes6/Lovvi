package com.lovvi.dto;

import java.math.BigDecimal;

public record LovviMatchCrudRequest(
        int idUsuario1,
        int idUsuario2,
        BigDecimal compatibilidade,
        String statusMatch
) {
}
