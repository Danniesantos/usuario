package com.daniela.usuario.converter;

import com.daniela.usuario.business.converter.UsuarioConverter;
import com.daniela.usuario.business.dto.EnderecoDTO;
import com.daniela.usuario.business.dto.TelefoneDTO;
import com.daniela.usuario.business.dto.UsuarioDTO;
import com.daniela.usuario.infrastructure.entity.Endereco;
import com.daniela.usuario.infrastructure.entity.Telefone;
import com.daniela.usuario.infrastructure.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class UsuarioConverterTest {

    @InjectMocks
    UsuarioConverter usuarioConverter;

    @Mock
    Usuario usuario;
    Endereco endereco;
    Telefone telefone;
    UsuarioDTO usuarioDTO;
    EnderecoDTO enderecoDTO;
    TelefoneDTO telefoneDTO;


    @BeforeEach
    void setup() {
        endereco = Endereco.builder()
                .rua("Rua das Flores")
                .numero(3L)
                .cep("13450-000")
                .complemento("Logo ali")
                .cidade("São Paulo")
                .estado("SP")
                .build();
        telefone = Telefone.builder()
                .ddd("11")
                .numero("123456789")
                .build();
        usuario = Usuario.builder()
                .nome("test")
                .email("email@gmail.com")
                .senha("adm123")
                .enderecos(List.of(endereco))
                .telefones(List.of(telefone))
                .build();

        enderecoDTO = EnderecoDTO.builder()
                .id(1L)
                .rua("Rua das Flores")
                .numero(3L)
                .cep("13450-000")
                .complemento("Logo ali")
                .cidade("São Paulo")
                .estado("SP")
                .build();
        telefoneDTO = TelefoneDTO.builder()
                .id(1L)
                .ddd("11")
                .numero("123456789")
                .build();
        usuarioDTO = UsuarioDTO.builder()
                .nome("test")
                .email("email@gmail.com")
                .senha("adm123")
                .enderecos(List.of(enderecoDTO))
                .telefones(List.of(telefoneDTO))
                .build();

    }

    @Test
    void shouldConverterUserSuccessfully() {
        Usuario entity = usuarioConverter.paraUsuario(usuarioDTO);

        assertThat(entity).isEqualTo(usuario);
    }

    @Test
    void shouldConverterUserDTOSuccessfully() {
        UsuarioDTO dto = usuarioConverter.paraUsuarioDTO(usuario);

        assertThat(dto).isEqualTo(usuarioDTO);
    }

    @Test
    void shouldConverterAddressEntitySuccessfully() {
        Endereco address = usuarioConverter.paraEndereco(enderecoDTO);

        assertThat(address).isEqualTo(endereco);
    }

   
    @Test
    void shouldConverterPhoneEntitySuccessfully() {
        Telefone phone = usuarioConverter.paraTelefone(telefoneDTO);

        assertThat(phone).isEqualTo(telefone);
    }


}
