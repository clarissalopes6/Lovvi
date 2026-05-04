package com.lovvi.model;

import java.time.LocalDate;

public record Foto(int numFoto, String urlFoto, LocalDate dataUpload, int idUsuario) {
}
