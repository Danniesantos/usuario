package com.daniela.usuario.business;

import com.daniela.usuario.business.converter.UsuarioConverter;
import com.daniela.usuario.business.dto.EnderecoDTO;
import com.daniela.usuario.business.dto.TelefoneDTO;
import com.daniela.usuario.business.dto.UsuarioDTO;
import com.daniela.usuario.infrastructure.entity.Endereco;
import com.daniela.usuario.infrastructure.entity.Telefone;
import com.daniela.usuario.infrastructure.entity.Usuario;
import com.daniela.usuario.infrastructure.exceptions.ConflictException;
import com.daniela.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.daniela.usuario.infrastructure.exceptions.UnauthorizedException;
import com.daniela.usuario.infrastructure.repository.EnderecoRepository;
import com.daniela.usuario.infrastructure.repository.TelefoneRepository;
import com.daniela.usuario.infrastructure.repository.UsuarioRepository;
import com.daniela.usuario.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    UsuarioConverter usuarioConverter;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtUtil jwtUtil;
    @Mock
    EnderecoRepository enderecoRepository;
    @Mock
    TelefoneRepository telefoneRepository;

    @InjectMocks
    UsuarioService usuarioService;

    Usuario usuarioEntity;
    UsuarioDTO usuarioDTO;
    Endereco enderecoEntity;
    EnderecoDTO enderecoDTO;
    Telefone telefoneEntity;
    TelefoneDTO telefoneDTO;


    @BeforeEach
    void setup() {
        enderecoEntity = new Endereco(null, "rua das ameixas", 1L, "Logo ali",
                "São Paulo", "SP", "12340-000", null);
        telefoneEntity = new Telefone(null, "123456789", "11", null);
        usuarioEntity = new Usuario();
        usuarioEntity.setNome("testando");
        usuarioEntity.setEmail("email@gmail.com");
        usuarioEntity.setSenha("1234567");
        usuarioEntity.setEnderecos(List.of(enderecoEntity));
        usuarioEntity.setTelefones(List.of(telefoneEntity));

        enderecoDTO = new EnderecoDTO(null, "rua das ameixas", 1L, "Logo ali",
                "São Paulo", "SP", "12340-000");
        telefoneDTO = new TelefoneDTO(null, "123456789", "11");
        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setNome("testando");
        usuarioDTO.setEmail("email@gmail.com");
        usuarioDTO.setSenha("1234567");
        usuarioDTO.setEnderecos(List.of(enderecoDTO));
        usuarioDTO.setTelefones(List.of(telefoneDTO));

    }

    @Test
    @DisplayName("Should save user when email does not exist")
    void shouldSaveUserSuccessfully() {
        when(usuarioRepository.existsByEmail(usuarioDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encode123");
        when(usuarioConverter.paraUsuario(any())).thenReturn(usuarioEntity);
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        when(usuarioConverter.paraUsuarioDTO(any())).thenReturn(usuarioDTO);

        UsuarioDTO user = usuarioService.salvaUsuario(usuarioDTO);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(usuarioDTO.getEmail());
        verify(usuarioRepository).save(eq(usuarioEntity));

    }

    @Test
    @DisplayName("Should throw ConflictException when email already exists")
    void shouldThrowConflictWhenEmailExists() {
        when(usuarioRepository.existsByEmail(usuarioDTO.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.salvaUsuario(usuarioDTO))
                .isInstanceOf(ConflictException.class);

    }

    @Test
    @DisplayName("Should authenticate user and return JWT token")
    void shouldAuthenticateUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("teste@gmail.com");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(anyString())).thenReturn("token");

        String token = usuarioService.autenticarUsuario(usuarioDTO);

        assertThat(token).startsWith("Bearer");
        assertThat(token).contains("token");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when credentials invalid")
    void shouldThrowExceptionWhenAuthenticationFails() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credentials Inválid"));

        assertThatThrownBy(() -> usuarioService.autenticarUsuario(usuarioDTO))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Email não localizado ou inválido");


    }

    @Test
    @DisplayName("Should return user when find by email  ")
    void shouldReturnUserWhenFindEmail() {
        String email = "email@gmail.com";

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioConverter.paraUsuarioDTO(usuarioEntity)).thenReturn(usuarioDTO);

        UsuarioDTO found = usuarioService.buscarUsuarioPorEmail(email);

        assertThat(found.getEmail()).isEqualTo(email);
        assertThat(found).isNotNull();

        verify(usuarioRepository).findByEmail(email);
        verifyNoMoreInteractions(usuarioRepository);

    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when email invalid")
    void shouldThrowResourceNotFoundExceptionWhenEmailInvalid() {
        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarUsuarioPorEmail("invalidemail@gmail.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Email não localizado ou inválido");

        verify(usuarioRepository).findByEmail("invalidemail@gmail.com");
        verifyNoMoreInteractions(usuarioRepository);

    }

    @Test
    @DisplayName("Should delete user by email")
    void shouldDeleteUserByEmail() {
        doNothing().when(usuarioRepository).deleteByEmail(usuarioDTO.getEmail());

        usuarioService.deletaUsuarioPorEmail(usuarioDTO.getEmail());

        verify(usuarioRepository, times(1)).deleteByEmail(usuarioEntity.getEmail());

    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when delete user with email invalid")
    void shouldThrowResourceNotFoundExceptionWhenDeleteUserEmailInvalid() {
        doThrow(new ResourceNotFoundException("Email não localizado ou inválido"))
                .when(usuarioRepository).deleteByEmail(anyString());

        assertThatThrownBy(() -> usuarioService.deletaUsuarioPorEmail("invalid@gmail.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Email não localizado ou inválido");

        verify(usuarioRepository).deleteByEmail("invalid@gmail.com");
    }

    @Test
    @DisplayName("Should update user when token contains valid email ")
    void shouldUpdateUserWhenTokenContainsValidEmail() {
        String token = "Bearer abcdekl";
        String email = "generatedtoken@gmail.com";
        when(jwtUtil.extractUsername(token.substring(7))).thenReturn(email);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded123");
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioConverter.updateUsuario(usuarioDTO, usuarioEntity)).thenReturn(usuarioEntity);
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        when(usuarioConverter.paraUsuarioDTO(usuarioEntity)).thenReturn(usuarioDTO);

        UsuarioDTO result = usuarioService.atualizaDadosUsuario(token, usuarioDTO);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(usuarioDTO);

        verify(jwtUtil).extractUsername(token.substring(7));
        verify(passwordEncoder).encode(anyString());
        verify(usuarioRepository).findByEmail(email);
        verify(usuarioConverter).updateUsuario(usuarioDTO, usuarioEntity);
        verify(usuarioRepository).save(usuarioEntity);
        verify(usuarioConverter).paraUsuarioDTO(usuarioEntity);

    }

    @Test
    @DisplayName("Should throw resourceNotFoundException when user not found for email in token ")
    void shouldThrowExceptionWhenUserResourceNotFoundExceptionForEmailInToken() {
        String token = "Bearer abcdekl";
        String email = "notfound@gmail.com";

        when(jwtUtil.extractUsername(token.substring(7))).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.atualizaDadosUsuario(token, usuarioDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Email não localizado ou inválido");

        verify(usuarioRepository).findByEmail(email);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(usuarioConverter);
    }

    @Test
    @DisplayName("Should update address when idAddress contains valid id ")
    void shouldUpdateAddressWhenIdContainsValidId() {
        Long idAddress = 1L;
        when(enderecoRepository.findById(idAddress)).thenReturn(Optional.of(enderecoEntity));
        when(usuarioConverter.updateEndereco(enderecoDTO, enderecoEntity)).thenReturn(enderecoEntity);
        when(enderecoRepository.save(enderecoEntity)).thenReturn(enderecoEntity);
        when(usuarioConverter.paraEnderecoDTO(enderecoEntity)).thenReturn(enderecoDTO);

        EnderecoDTO update = usuarioService.atualizaEndereco(idAddress, enderecoDTO);

        assertThat(update).isNotNull();
        assertThat(update).isEqualTo(enderecoDTO);
        verify(enderecoRepository).findById(idAddress);
        verify(enderecoRepository).save(enderecoEntity);
        verify(usuarioConverter).updateEndereco(enderecoDTO, enderecoEntity);
        verify(usuarioConverter).paraEnderecoDTO(enderecoEntity);


    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException update address when idAddress contains invalid id ")
    void shouldThrowResourceNotFoundExceptionUpdateAddressWhenIdAddressContainsInvalidId() {
        Long idAddress = 1L;
        when(enderecoRepository.findById(idAddress)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.atualizaEndereco(idAddress, enderecoDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Id não encontrado ");

        verify(enderecoRepository).findById(idAddress);
        verifyNoMoreInteractions(enderecoRepository);
        verifyNoInteractions(usuarioConverter);
    }

    @Test
    @DisplayName("Should update phone when idPhone contains valid id ")
    void shouldUpdatePhoneWhenIdContainsValidId() {
        Long idPhone = 2L;
        when(telefoneRepository.findById(idPhone)).thenReturn(Optional.of(telefoneEntity));
        when(usuarioConverter.updateTelefone(telefoneDTO, telefoneEntity)).thenReturn(telefoneEntity);
        when(telefoneRepository.save(telefoneEntity)).thenReturn(telefoneEntity);
        when(usuarioConverter.paraTelefoneDTO(telefoneEntity)).thenReturn(telefoneDTO);

        TelefoneDTO update = usuarioService.atualizaTelefone(idPhone, telefoneDTO);

        assertThat(update).isNotNull();
        assertThat(update).isEqualTo(telefoneDTO);
        verify(telefoneRepository).findById(idPhone);
        verify(telefoneRepository).save(telefoneEntity);
        verify(usuarioConverter).updateTelefone(telefoneDTO, telefoneEntity);
        verify(usuarioConverter).paraTelefoneDTO(telefoneEntity);


    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException update phone when idPhone contains invalid id ")
    void shouldThrowResourceNotFoundExceptionUpdatePhoneWhenIdPhoneContainsInvalidId() {
        Long idPhone = 2L;
        when(telefoneRepository.findById(idPhone)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.atualizaTelefone(idPhone, telefoneDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Id não encontrado ");

        verify(telefoneRepository).findById(idPhone);
        verifyNoMoreInteractions(telefoneRepository);
        verifyNoInteractions(usuarioConverter);
    }

    @Test
    @DisplayName("Should register address when token contains valid email ")
    void shouldRegisterAddressWhenTokenContainsValidEmail() {
        String token = "Bearer abcdekl";
        String email = "generatedtoken@gmail.com";
        when(jwtUtil.extractUsername(token.substring(7))).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioConverter.paraEnderecoEntity(enderecoDTO, usuarioEntity.getId())).thenReturn(enderecoEntity);
        when(enderecoRepository.save(enderecoEntity)).thenReturn(enderecoEntity);
        when(usuarioConverter.paraEnderecoDTO(enderecoEntity)).thenReturn(enderecoDTO);

        EnderecoDTO result = usuarioService.cadastraEndereco(enderecoDTO, token);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(enderecoDTO);

        verify(jwtUtil).extractUsername(token.substring(7));
        verify(usuarioRepository).findByEmail(email);
        verify(usuarioConverter).paraEnderecoEntity(enderecoDTO, usuarioEntity.getId());
        verify(enderecoRepository).save(enderecoEntity);
        verify(usuarioConverter).paraEnderecoDTO(enderecoEntity);

    }


    @Test
    @DisplayName("Should throw ResourceNotFoundException when token contains invalid email ")
    void shouldThrowResourceNotFoundExceptionWhenTokenContainsInvalidEmail() {
        String token = "Bearer abcdekl";
        String email = "generatedtoken@gmail.com";
        when(jwtUtil.extractUsername(token.substring(7))).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> usuarioService.cadastraEndereco(enderecoDTO, token))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Email não localizado ou inválido");


        verify(jwtUtil).extractUsername(token.substring(7));
        verify(usuarioRepository).findByEmail(email);
        verifyNoMoreInteractions(usuarioRepository, jwtUtil);
        verifyNoInteractions(usuarioConverter, enderecoRepository);

    }

    @Test
    @DisplayName("Should register phone when token contains valid email ")
    void shouldRegisterPhoneWhenTokenContainsValidEmail() {
        String token = "Bearer abcdekl";
        String email = "generatedtoken@gmail.com";
        when(jwtUtil.extractUsername(token.substring(7))).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioConverter.paraTelefoneEntity(telefoneDTO, usuarioEntity.getId())).thenReturn(telefoneEntity);
        when(telefoneRepository.save(telefoneEntity)).thenReturn(telefoneEntity);
        when(usuarioConverter.paraTelefoneDTO(telefoneEntity)).thenReturn(telefoneDTO);

        TelefoneDTO result = usuarioService.cadastraTelefone(telefoneDTO, token);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(telefoneDTO);

        verify(jwtUtil).extractUsername(token.substring(7));
        verify(usuarioRepository).findByEmail(email);
        verify(usuarioConverter).paraTelefoneEntity(telefoneDTO, usuarioEntity.getId());
        verify(telefoneRepository).save(telefoneEntity);
        verify(usuarioConverter).paraTelefoneDTO(telefoneEntity);

    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when register phone token contains invalid email ")
    void shouldThrowResourceNotFoundExceptionWhenTokenContainsInvalidEmailInPhone() {
        String token = "Bearer abcdekl";
        String email = "generatedtoken@gmail.com";
        when(jwtUtil.extractUsername(token.substring(7))).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> usuarioService.cadastraTelefone(telefoneDTO, token))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Email não localizado ou inválido");


        verify(jwtUtil).extractUsername(token.substring(7));
        verify(usuarioRepository).findByEmail(email);
        verifyNoMoreInteractions(usuarioRepository, jwtUtil);
        verifyNoInteractions(usuarioConverter, telefoneRepository);

    }


}
