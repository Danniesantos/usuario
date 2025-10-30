package com.daniela.usuario.controller;

import com.daniela.usuario.business.UsuarioService;
import com.daniela.usuario.business.ViaCepService;
import com.daniela.usuario.business.dto.EnderecoDTO;
import com.daniela.usuario.business.dto.TelefoneDTO;
import com.daniela.usuario.business.dto.UsuarioDTO;
import com.daniela.usuario.infrastructure.exceptions.ConflictException;
import com.daniela.usuario.infrastructure.exceptions.IllegalArgumentException;
import com.daniela.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.daniela.usuario.infrastructure.exceptions.UnauthorizedException;
import com.daniela.usuario.infrastructure.security.client.ViaCepDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@ExtendWith(SpringExtension.class)
public class UsuarioControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UsuarioService usuarioService;
    @MockitoBean
    private ViaCepService viaCepService;

    private UsuarioDTO usuarioDTO;
    private EnderecoDTO enderecoDTO;
    private TelefoneDTO telefoneDTO;
    private ViaCepDTO viaCepDTO;
    private String url;
    private String json;
    private String token;

    @BeforeEach
    void setup() throws JsonProcessingException {
        url = "/usuario";
        enderecoDTO = new EnderecoDTO(null, "Rua: das laranjeiras", 50L, "ultima casa",
                "São Paulo", "SP", "12345-000");
        telefoneDTO = new TelefoneDTO(null, "987654321", "11");
        usuarioDTO = new UsuarioDTO("teste", "email@gmail.com", "abcd1234",
                List.of(enderecoDTO), List.of(telefoneDTO));
        viaCepDTO = new ViaCepDTO("12345-000", "Rua Rio de Janeiro", "Casa Amarela", "1",
                "Aparecida", "Zona Sul", "RJ", "Rio de Janeiro", "Norte", "aaa",
                "", "333", "");
        json = objectMapper.writeValueAsString(usuarioDTO);
        token = "Bearer yyyy";
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() throws Exception {
        given(usuarioService.salvaUsuario(any(UsuarioDTO.class))).willReturn(usuarioDTO);

        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andDo(print())
                .andExpect(jsonPath("$.email").value(usuarioDTO.getEmail()));


        then(usuarioService).should().salvaUsuario(any(UsuarioDTO.class));
        then(usuarioService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("should throw ConflictException when email exists")
    void shouldThrowConflictExceptionWhenEmailExists() throws Exception {
        given(usuarioService.salvaUsuario(any(UsuarioDTO.class)))
                .willThrow(new ConflictException("Email já cadastrado"));

        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Email já cadastrado")));

        then(usuarioService).should().salvaUsuario(any(UsuarioDTO.class));
        then(usuarioService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should login when credentials valid")
    void shouldLoginWhenCredentialsValid() throws Exception {
        given(usuarioService.autenticarUsuario(any(UsuarioDTO.class))).willReturn(token);

        mockMvc.perform(post(url + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk());

        then(usuarioService).should().autenticarUsuario(any(UsuarioDTO.class));
        then(usuarioService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should throw UnauthorizedException  when credentials invalid")
    void shouldThrowLoginWhenCredentialsInvalid() throws Exception {
        given(usuarioService.autenticarUsuario(any(UsuarioDTO.class)))
                .willThrow(new UnauthorizedException("Crededials Invalid"));

        mockMvc.perform(post(url + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        then(usuarioService).should().autenticarUsuario(any(UsuarioDTO.class));
        then(usuarioService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should delete user by email successfully ")
    void shouldDeleteUserByEmailSuccessfully() throws Exception {
        willDoNothing().given(usuarioService).deletaUsuarioPorEmail(usuarioDTO.getEmail());

        mockMvc.perform(delete(url + "/{email}", usuarioDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        then(usuarioService).should().deletaUsuarioPorEmail(usuarioDTO.getEmail());
        then(usuarioService).shouldHaveNoMoreInteractions();

    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when email doesn't exist")
    void shouldThrowResourceNotFoundExceptionWhenEmailDoesNotExistOrIncorrect() throws Exception {
        willThrow(new ResourceNotFoundException("Email não localizado ou inválido"))
                .given(usuarioService)
                .deletaUsuarioPorEmail(usuarioDTO.getEmail());

        mockMvc.perform(delete(url + "/{email}", usuarioDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Email não localizado ou inválido")));

        then(usuarioService).should().deletaUsuarioPorEmail(usuarioDTO.getEmail());
        then(usuarioService).shouldHaveNoMoreInteractions();

    }

    @Test
    @DisplayName("Should find user when email is correct")
    void shouldFindUserWhenEmailIsCorrect() throws Exception {
        given(usuarioService.buscarUsuarioPorEmail(usuarioDTO.getEmail())).willReturn(usuarioDTO);

        mockMvc.perform(get(url)
                        .param("email", usuarioDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN")))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.email").value(usuarioDTO.getEmail()));

        then(usuarioService).should().buscarUsuarioPorEmail(usuarioDTO.getEmail());
        then(usuarioService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when email is not correct")
    void shouldThrowResourceNotFoundExceptionWhenEmailIsNotCorrect() throws Exception {
        given(usuarioService.buscarUsuarioPorEmail(usuarioDTO.getEmail()))
                .willThrow(new ResourceNotFoundException("Email não localizado ou inválido"));

        mockMvc.perform(get(url)
                        .param("email", usuarioDTO.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Email não localizado ou inválido")));

        then(usuarioService).should().buscarUsuarioPorEmail(usuarioDTO.getEmail());
        then(usuarioService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should return user data  when valid token is provided")
    void shouldReturnUserDataWhenValidTokenIsProvided() throws Exception {

        given(usuarioService.atualizaDadosUsuario(anyString(), any(UsuarioDTO.class)))
                .willReturn(usuarioDTO);

        mockMvc.perform(put(url)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.email").value(usuarioDTO.getEmail()));

        then(usuarioService).should().atualizaDadosUsuario(anyString(), any(UsuarioDTO.class));
        then(usuarioService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should return 401 when token is invalid")
    void shouldReturn401WhenTokenIsInvalid() throws Exception {

        mockMvc.perform(put(url)
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should Update Address When Id Is Provided")
    void shouldUpdateAddressWhenIdIsProvided() throws Exception {

        given(usuarioService.atualizaEndereco(anyLong(), any(EnderecoDTO.class)))
                .willReturn(enderecoDTO);

        mockMvc.perform(put(url + "/endereco")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk());

        then(usuarioService).should().atualizaEndereco(anyLong(), any(EnderecoDTO.class));
        then(usuarioService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should return 404 when id not found")
    void shouldReturn404WhenIdNotFound() throws Exception {

        given(usuarioService.atualizaEndereco(anyLong(), any(EnderecoDTO.class)))
                .willThrow(new ResourceNotFoundException("Id não encontrado"));

        mockMvc.perform(put(url + "/endereco")
                        .param("id", "99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Id não encontrado")));

        then(usuarioService).should().atualizaEndereco(anyLong(), any(EnderecoDTO.class));
        then(usuarioService).shouldHaveNoMoreInteractions();

    }


    @Test
    @DisplayName("Should update phone When Id is provided")
    void shouldUpdatePhoneWhenIdIsProvided() throws Exception {

        given(usuarioService.atualizaTelefone(anyLong(), any(TelefoneDTO.class)))
                .willReturn(telefoneDTO);

        mockMvc.perform(put(url + "/telefone")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk());

        then(usuarioService).should().atualizaTelefone(anyLong(), any(TelefoneDTO.class));
        then(usuarioService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should register address when valid token is provided")
    void shouldRegisterAddressWhenValidTokenIsProvided() throws Exception {
        given(usuarioService.cadastraEndereco(any(EnderecoDTO.class), anyString())).willReturn(enderecoDTO);

        mockMvc.perform(post(url + "/endereco")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user("admin").password("123").roles("ADMIN")))
                .andExpect(status().isCreated());

        then(usuarioService).should().cadastraEndereco(any(EnderecoDTO.class), anyString());
        then(usuarioService).shouldHaveNoMoreInteractions();


    }

    @Test
    @DisplayName("Should register phone when valid token is provided")
    void shouldRegisterPhoneWhenValidTokenIsProvided() throws Exception {
        given(usuarioService.cadastraTelefone(any(TelefoneDTO.class), anyString())).willReturn(telefoneDTO);

        mockMvc.perform(post(url + "/telefone")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user("admin").password("123").roles("ADMIN")))
                .andExpect(status().isCreated());

        then(usuarioService).should().cadastraTelefone(any(TelefoneDTO.class), anyString());
        then(usuarioService).shouldHaveNoMoreInteractions();


    }

    @Test
    @DisplayName("Should return address when postal is correct")
    void shouldReturnAddressWhenPostalIsCorrect() throws Exception {
        given(viaCepService.buscaDadosCep(anyString())).willReturn(viaCepDTO);

        mockMvc.perform(get(url + "/endereco/{cep}", viaCepDTO.getCep())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("admin").password("123").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk());

        then(viaCepService).should().buscaDadosCep(anyString());
        then(viaCepService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Should return 400 Bad Request when postal code is invalid")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturn400BadRequestWhenPostalIsInvalid() throws Exception {
        given(viaCepService.buscaDadosCep(anyString()))
                .willThrow(new IllegalArgumentException("O cep contém caracteres inválidos"));

        mockMvc.perform(get(url + "/endereco/{cep}", "123456789")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "O cep contém caracteres inválidos"));


    }


}
