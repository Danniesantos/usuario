package com.daniela.usuario.dto;

import com.daniela.usuario.business.dto.EnderecoDTO;
import com.daniela.usuario.business.dto.TelefoneDTO;
import com.daniela.usuario.business.dto.UsuarioDTO;

import java.util.List;

public class UsuarioDTOFixture {
    public static UsuarioDTO build(
            String nome,
            String email,
            String senha,
            List<EnderecoDTO> enderecos,
            List<TelefoneDTO> telefones) {
        return new UsuarioDTO(nome, email, senha, enderecos, telefones);
    }
}