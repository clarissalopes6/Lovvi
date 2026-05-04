package com.lovvi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LovviMatch(
        int idMatch,
        int idUsuario1,
        int idUsuario2,
        LocalDate dataMatch,
        BigDecimal compatibilidade,
        String statusMatch
) {
}
