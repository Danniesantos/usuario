package com.daniela.usuario.business;

import com.daniela.usuario.infrastructure.exceptions.IllegalArgumentException;
import com.daniela.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.daniela.usuario.infrastructure.security.client.ViaCepClient;
import com.daniela.usuario.infrastructure.security.client.ViaCepDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ViaCepServiceTest {

    @InjectMocks
    private ViaCepService viaCepService;

    @Mock
    private ViaCepClient viaCepClient;

    ViaCepDTO viaCepDTO;
    ViaCepDTO viaCepDTO2;


    @BeforeEach
    void setup() {
        viaCepDTO = new ViaCepDTO("12345000", "Rua Rio de Janeiro", "Casa Amarela",
                "1", "Aparecida", "Zona Sul", "RJ", "Rio de Janeiro",
                "Norte", "aaa", "", "333", "");

        viaCepDTO2 = new ViaCepDTO("12345-000", "Rua Rio de Janeiro", "Casa Amarela",
                "1", "Aparecida", "Zona Sul", "RJ", "Rio de Janeiro",
                "Norte", "aaa", "", "333", "");
    }

    @Test
    @DisplayName("Should Return new Address When Cep is Válid")
    void shouldReturnAddressWhenCepValid() {
        when(viaCepClient.buscaDadosEndereco(viaCepService.processarCep(viaCepDTO.getCep())))
                .thenReturn(viaCepDTO);

        var returnCep = viaCepService.buscaDadosCep(viaCepDTO.getCep());

        assertThat(returnCep).isNotNull();

        verify(viaCepClient).buscaDadosEndereco(viaCepDTO.getCep());
        verifyNoMoreInteractions(viaCepClient);


    }

    @Test
    @DisplayName("Should IllegalArgumentException When Cep is Inválid")
    void shouldIllegalArgumentExceptionWhenCepInvalid() {
        when(viaCepClient.buscaDadosEndereco(viaCepService.processarCep(viaCepDTO2.getCep())))
                .thenThrow(new IllegalArgumentException("O cep contém caracteres inválidos"));

        assertThatThrownBy(() -> viaCepService.buscaDadosCep(viaCepDTO.getCep()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O cep contém caracteres inválidos");


    }

}
