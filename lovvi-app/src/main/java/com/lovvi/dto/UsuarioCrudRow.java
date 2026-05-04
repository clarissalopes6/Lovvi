package com.lovvi.dto;

import java.time.LocalDate;

public record UsuarioCrudRow(
        int idUsuario,
        String nome,
        String sobrenome,
        String email,
        String cidade,
        String genero,
        LocalDate dtNascimento
) {
}
