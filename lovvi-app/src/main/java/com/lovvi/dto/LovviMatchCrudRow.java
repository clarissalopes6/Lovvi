package com.lovvi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LovviMatchCrudRow(
        int idMatch,
        int idUsuario1,
        String usuario1,
        int idUsuario2,
        String usuario2,
        LocalDate dataMatch,
        BigDecimal compatibilidade,
        String statusMatch
) {
}
