package com.lovvi.dto;

import java.time.LocalDate;

public record UsuarioCrudRequest(
        String nome,
        String sobrenome,
        String email,
        String senha,
        String cidade,
        String genero,
        LocalDate dtNascimento
) {
}
