package com.lovvi.model;

import java.time.LocalDate;

public record Usuario(
        int idUsuario,
        String nome,
        String sobrenome,
        String email,
        String senha,
        String cidade,
        String genero,
        LocalDate dtNascimento
) {
}
