package com.daniela.usuario.business;

import com.daniela.usuario.infrastructure.security.client.ViaCepClient;
import com.daniela.usuario.infrastructure.security.client.ViaCepDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ViaCepService {

    private final ViaCepClient viaCepClient;

    public ViaCepDTO buscaDadosCep(String cep) {
        try {
            return viaCepClient.buscaDadosEndereco(processarCep(cep));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Erro: ", e);
        }
    }

    public String processarCep(String cep) {
        String cepFormatado = cep.replace(" ", "").replace("-", "");

        if (!cepFormatado.matches("\\d+")
                || !Objects.equals(cepFormatado.length(), 8)) {
            throw new IllegalArgumentException("O cep contém caracteres inválidos, por favor verificar");
        }
        return cepFormatado;
    }
}
