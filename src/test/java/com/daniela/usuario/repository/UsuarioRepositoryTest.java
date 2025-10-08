package com.daniela.usuario.repository;

import com.daniela.usuario.infrastructure.entity.Endereco;
import com.daniela.usuario.infrastructure.entity.Telefone;
import com.daniela.usuario.infrastructure.entity.Usuario;
import com.daniela.usuario.infrastructure.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UsuarioRepositoryTest {

    @Autowired
    UsuarioRepository usuarioRepository;

    Usuario usuario;
    Usuario usuario1;

    @BeforeEach
    void setup() {
        Endereco endereco1 = new Endereco(null, "rua das ameixas", 1L, "Logo ali",
                "São Paulo", "SP", "12340-000", null);
        Endereco endereco2 = new Endereco(null, "rua das laranjas", 2L, "casa verde",
                "Rio de janeiro", "RJ", "13540-000", null);
        Telefone telefone = new Telefone(null, "123456789", "11", null);
        usuario = new Usuario();
        usuario.setNome("testando");
        usuario.setEmail("email@gmail.com");
        usuario.setSenha("1234567");
        usuario.setEnderecos(List.of(endereco1, endereco2));
        usuario.setTelefones(List.of(telefone));

        usuario1 = new Usuario();
        usuario1.setNome("test");
        usuario1.setEmail("teste@gmail.com");
        usuario1.setSenha("123456");
        usuario1.setEnderecos(new ArrayList<>());
        usuario1.setTelefones(new ArrayList<>());

    }

    @Test
    @DisplayName("Should save user with multiple addresses and phone numbers")
    void shouldSaveNewUserWithAddressesAndPhones() {
        Usuario savedUsuario = usuarioRepository.save(usuario);

        assertThat(savedUsuario).isNotNull();
        assertThat(savedUsuario.getId()).isNotNull();
        assertThat(savedUsuario.getId()).isGreaterThan(0L);
        assertThat(savedUsuario.getEnderecos()).hasSize(2);
        assertThat(savedUsuario.getTelefones()).hasSize(1);

    }

    @Test
    @DisplayName("Should save user with empty addresses and phone numbers")
    void shouldSaveNewUserWithEmptyAddressesAndPhones() {
        Usuario savedUsuario = usuarioRepository.save(usuario1);

        assertThat(savedUsuario).isNotNull();
        assertThat(savedUsuario.getEnderecos()).isEmpty();
        assertThat(savedUsuario.getTelefones()).isEmpty();
    }

    @Test
    @DisplayName("Should find user for email")
    void shouldFindUserByEmail() {
        Usuario savedUser = usuarioRepository.save(usuario);
        Optional<Usuario> found = usuarioRepository.findByEmail(savedUser.getEmail());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(usuario.getEmail());

    }

    @Test
    @DisplayName("Should delete User by Id")
    void shouldDeleteUserById() {
        Usuario savedUser = usuarioRepository.save(usuario);
        usuarioRepository.deleteById(savedUser.getId());

        Optional<Usuario> found = usuarioRepository.findById(savedUser.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should Update email by id in User")
    void shouldUpdateEmailUserById() {
        Usuario savedUser = usuarioRepository.save(usuario1);
        Long id = savedUser.getId();

        savedUser.setEmail("test@gmail.com");
        Usuario updateUser = usuarioRepository.save(savedUser);

        assertThat(updateUser).isNotNull();
        assertThat(updateUser.getId()).isEqualTo(id);
        assertThat(updateUser.getEmail()).isEqualTo("test@gmail.com");


    }
}
