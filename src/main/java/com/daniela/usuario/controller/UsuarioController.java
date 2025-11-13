package com.daniela.usuario.controller;

import com.daniela.usuario.business.UsuarioService;
import com.daniela.usuario.business.ViaCepService;
import com.daniela.usuario.business.dto.EnderecoDTO;
import com.daniela.usuario.business.dto.TelefoneDTO;
import com.daniela.usuario.business.dto.UsuarioDTO;
import com.daniela.usuario.infrastructure.security.SecurityConfig;
import com.daniela.usuario.infrastructure.security.client.ViaCepDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/usuario")
@Tag(name = "usuario", description = "Controlador para salvar e editar dados de usuarios")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME)
public class UsuarioController {
    private final UsuarioService usuarioService;

    private final ViaCepService viaCepService;

    @PostMapping
    @Operation(summary = "Salva dados de usuário", description = "Método para salvar dados de usuário")
    @ApiResponse(responseCode = "201", description = "Usuario criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Email já cadastrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<UsuarioDTO> salvaUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.salvaUsuario(usuarioDTO));
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "Método que faz login de usuário")
    @ApiResponse(responseCode = "200", description = "Usuario logado com sucesso ")
    @ApiResponse(responseCode = "400", description = "Usuario não encontrado, email ou senha inválidos")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<String> login(@RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.autenticarUsuario(usuarioDTO));
    }

    @GetMapping
    @Operation(summary = "Busca usuario por email", description = "Busca usuario pelo email")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Email não encontrado ou cadastrado")
    @ApiResponse(responseCode = "500", description = "Erro de servidor")
    public ResponseEntity<UsuarioDTO> buscaUsuarioPorEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorEmail(email));
    }

    @Operation(summary = "Deleta usuario por email", description = "Faz a deleção de um usuario pelo email")
    @ApiResponse(responseCode = "204", description = "Usuario excluido com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuario não encontrado, email incorreto ou não existente")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deletaUsuarioPorEmail(@PathVariable String email) {
        usuarioService.deletaUsuarioPorEmail(email);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    @Operation(summary = "Atualiza dados de usuario", description = "Faz a atualização de usuario pelo token gerado")
    @ApiResponse(responseCode = "200", description = "Usuario atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuario não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<UsuarioDTO> atualizaDadosUsuario(@RequestBody UsuarioDTO usuarioDTO,
                                                           @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(usuarioService.atualizaDadosUsuario(token, usuarioDTO));
    }

    @Operation(summary = "Atualiza endereço do usuario",
            description = "Busca o usuario pelo id e atualiza o endereço do usuario ")
    @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuario não encontrado, id incorreto ou inexistente")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PutMapping("/endereco")
    public ResponseEntity<EnderecoDTO> atualizaEndereco(@RequestBody EnderecoDTO enderecoDTO,
                                                        @RequestParam("id") Long id) {
        return ResponseEntity.ok(usuarioService.atualizaEndereco(id, enderecoDTO));
    }

    @Operation(summary = "Atualiza telefone do usuario",
            description = "Busca o usuario pelo id e atualiza o telefone do usuario ")
    @ApiResponse(responseCode = "200", description = "Telefone atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuario não encontrado, id incorreto ou inexistente")
    @ApiResponse(responseCode = "500", description = "Erro de servidor")
    @PutMapping("/telefone")
    public ResponseEntity<TelefoneDTO> atualizaTelefone(@RequestBody TelefoneDTO telefoneDTO,
                                                        @RequestParam("id") Long id) {
        return ResponseEntity.ok(usuarioService.atualizaTelefone(id, telefoneDTO));
    }

    @Operation(summary = "Cadastra um novo endereço de usuario",
            description = "Cadatra um novo endereço do usuario com o token gerado")
    @ApiResponse(responseCode = "201", description = "Novo endereço salvo com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuario não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro de servidor")
    @PostMapping("/endereco")
    public ResponseEntity<EnderecoDTO> cadastraEndereco(@RequestBody EnderecoDTO enderecoDTO,
                                                        @RequestHeader("Authorization") String token) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.cadastraEndereco(enderecoDTO, token));
    }

    @Operation(summary = "Cadastra um novo telefone de usuario",
            description = "Cadatra um novo telefone do usuario com o token gerado")
    @ApiResponse(responseCode = "201", description = "Novo telefone salvo com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuario não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro de servidor")
    @PostMapping("/telefone")
    public ResponseEntity<TelefoneDTO> cadastraTelefone(@RequestBody TelefoneDTO telefoneDTO,
                                                        @RequestHeader("Authorization") String token) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.cadastraTelefone(telefoneDTO, token));
    }

    @Operation(summary = "Localiza endereço pelo cep",
            description = "Localiza e faz uma busca por endereços pelo cep")
    @ApiResponse(responseCode = "200", description = "Endereço encontrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Cep inválido ou incorreto")
    @ApiResponse(responseCode = "500", description = "Erro de servidor")
    @GetMapping("/endereco/{cep}")
    public ResponseEntity<ViaCepDTO> buscaDadosCep(@PathVariable("cep") String cep) {
        return ResponseEntity.ok(viaCepService.buscaDadosCep(cep));
    }
}
