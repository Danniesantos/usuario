package com.daniela.usuario.dto;

import com.daniela.usuario.business.dto.TelefoneDTO;

public class TelefoneDTOFixture {
    public static TelefoneDTO build(
            Long id,
            String numero,
            String ddd) {
        return new TelefoneDTO(id, numero, ddd);
    }
}