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
import com.daniela.usuario.infrastructure.repository.EnderecoRepository;
import com.daniela.usuario.infrastructure.repository.TelefoneRepository;
import com.daniela.usuario.infrastructure.repository.UsuarioRepository;
import com.daniela.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO) {
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste(String email) {
        try {
            boolean existe = verificaEmailExistente(email);
            if (existe) {
                throw new ConflictException("Email já cadastrado " + email);
            }
        } catch (ConflictException e) {
            throw new ConflictException("Email já cadastrado " + e.getCause());
        }
    }

    public boolean verificaEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public UsuarioDTO buscarUsuarioPorEmail(String email) {
        try {
            return usuarioConverter.paraUsuarioDTO(
                    usuarioRepository.findByEmail(email).orElseThrow(
                    () -> new ResourceNotFoundException("Email não encontrado " + email)));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Email não localizado " + email);
        }
    }
    public void deletaUsuarioPorEmail(String email){
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto){
        String email = jwtUtil.extractUsername(token.substring(7));

        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null );

        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado "));

        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);

        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO){

        Endereco entity = enderecoRepository.findById(idEndereco).orElseThrow(
                () -> new ResourceNotFoundException("Id não encontrado " + idEndereco));
        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity);

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO telefoneDTO){

        Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow(
                () -> new ResourceNotFoundException("Id não encontrado " + idTelefone));

        Telefone telefone = usuarioConverter.updateTelefone(telefoneDTO, entity);

        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTO cadastraEndereco(EnderecoDTO dto, String token){

        String email = jwtUtil.extractUsername(token.substring(7));

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                ()-> new ResourceNotFoundException("Email não localizado " + email));

        Endereco endereco = usuarioConverter.paraEnderecoEntity(dto, usuario.getId());

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));

    }

    public TelefoneDTO cadastraTelefone(TelefoneDTO dto, String token){

        String email = jwtUtil.extractUsername(token.substring(7));

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                ()-> new ResourceNotFoundException("Email não localizado " + email));

        Telefone telefone = usuarioConverter.paraTelefoneEntity(dto, usuario.getId());

        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }
}
