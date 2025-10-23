package com.daniela.usuario.dto;

import com.daniela.usuario.business.dto.EnderecoDTO;

public class EnderecoDTOFixture {
    public static EnderecoDTO build(
            Long id,
            String rua,
            Long numero,
            String complemento,
            String cidade,
            String estado,
            String cep) {
        return new EnderecoDTO(id, rua, numero, complemento, cidade, estado, cep);
    }
}