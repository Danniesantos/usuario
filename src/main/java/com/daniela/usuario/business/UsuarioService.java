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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    public static final String ERRO_EMAIL = "Email não localizado ou inválido";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO) {
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public String autenticarUsuario(UsuarioDTO usuarioDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usuarioDTO.getEmail(),
                            usuarioDTO.getSenha())
            );
            return "Bearer" + jwtUtil.generateToken(authentication.getName());
        } catch (BadCredentialsException | UsernameNotFoundException | AuthorizationDeniedException e) {
            throw new UnauthorizedException(ERRO_EMAIL, e.getCause());
        }

    }

    public void emailExiste(String email) {
        try {
            boolean existe = verificaEmailExistente(email);
            if (existe) {
                throw new ConflictException("Email já cadastrado " + email);
            }
        } catch (ConflictException e) {
            throw new ConflictException(ERRO_EMAIL + e.getCause());
        }
    }

    public boolean verificaEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public UsuarioDTO buscarUsuarioPorEmail(String email) {
        try {
            return usuarioConverter.paraUsuarioDTO(
                    usuarioRepository.findByEmail(email).orElseThrow(
                            () -> new ResourceNotFoundException(ERRO_EMAIL + email)));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(ERRO_EMAIL + email);
        }
    }

    public void deletaUsuarioPorEmail(String email) {
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO usuarioDTO) {
        String email = jwtUtil.extractUsername(token.substring(7));

        usuarioDTO.setSenha(usuarioDTO.getSenha() != null ?
                passwordEncoder.encode(usuarioDTO.getSenha()) : null);

        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException(ERRO_EMAIL));

        Usuario usuario = usuarioConverter.updateUsuario(usuarioDTO, usuarioEntity);

        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO) {

        Endereco entity = enderecoRepository.findById(idEndereco).orElseThrow(
                () -> new ResourceNotFoundException(ERRO_EMAIL + idEndereco));
        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity);

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO telefoneDTO) {

        Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow(
                () -> new ResourceNotFoundException("Id não encontrado " + idTelefone));

        Telefone telefone = usuarioConverter.updateTelefone(telefoneDTO, entity);

        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTO cadastraEndereco(EnderecoDTO enderecoDTO, String token) {

        String email = jwtUtil.extractUsername(token.substring(7));

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException(ERRO_EMAIL + email));

        Endereco endereco = usuarioConverter.paraEnderecoEntity(enderecoDTO, usuario.getId());

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));

    }

    public TelefoneDTO cadastraTelefone(TelefoneDTO telefoneDTO, String token) {

        String email = jwtUtil.extractUsername(token.substring(7));

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException(ERRO_EMAIL + email));

        Telefone telefone = usuarioConverter.paraTelefoneEntity(telefoneDTO, usuario.getId());

        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }
}
