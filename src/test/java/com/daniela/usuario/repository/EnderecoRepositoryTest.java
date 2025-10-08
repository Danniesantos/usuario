package com.daniela.usuario.repository;

import com.daniela.usuario.infrastructure.entity.Endereco;
import com.daniela.usuario.infrastructure.repository.EnderecoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class EnderecoRepositoryTest {

    @Autowired
    EnderecoRepository enderecoRepository;

    Endereco endereco1;
    Endereco endereco2;

    @BeforeEach
    void setup() {
        endereco1 = new Endereco(null, "rua das ameixas", 1L, "Logo ali",
                "São Paulo", "SP", "12340-000", null);
        endereco2 = new Endereco(null, "rua das laranjas", 2L, "casa verde",
                "Rio de janeiro", "RJ", "13540-000", null);

    }

    @Test
    @DisplayName("Should save address in DB")
    void shouldSaveAddress() {
        Endereco savedEndereco = enderecoRepository.save(endereco1);

        assertThat(savedEndereco).isNotNull();
        assertThat(savedEndereco.getId()).isNotNull();
        assertThat(savedEndereco.getId()).isGreaterThan(0L);

    }

    @Test
    @DisplayName("Should save multiples addresses in DB")
    void shouldSaveAddresses() throws IndexOutOfBoundsException {
        List<Endereco> adresses = List.of(endereco1, endereco2);

        List<Endereco> savedEnderecos = enderecoRepository.saveAll(adresses);

        assertThat(savedEnderecos).isNotNull();
        assertThat(savedEnderecos).hasSize(2);
        assertThat(savedEnderecos.get(0).getCidade()).isEqualTo("São Paulo");
        assertThat(savedEnderecos.get(1).getCidade()).isEqualTo("Rio de janeiro");

    }

    @Test
    @DisplayName("Should find multiples addresses in DB")
    void shouldFindAddressesById() {
        List<Endereco> adresses = List.of(endereco1, endereco2);
        enderecoRepository.saveAll(adresses);

        List<Endereco> foundAll = enderecoRepository.findAll();

        assertThat(foundAll).isNotNull();
        assertThat(foundAll).hasSize(2);


    }

    @Test
    @DisplayName("Should delete address by Id ")
    void shouldDeleteAddressById() {
        Endereco adress = enderecoRepository.save(endereco1);
        enderecoRepository.deleteById(adress.getId());

        Optional<Endereco> found = enderecoRepository.findById(adress.getId());

        assertThat(found).isEmpty();


    }

    @Test
    @DisplayName("Should delete one address by Id ")
    void shouldDeleteOneAddressById() {
        List<Endereco> adresses = List.of(endereco1, endereco2);
        List<Endereco> savedAdress = enderecoRepository.saveAll(adresses);

        assertThat(savedAdress).hasSize(2);

        Long idToDelete = savedAdress.get(0).getId();
        enderecoRepository.deleteById(idToDelete);

        Optional<Endereco> deletedAdress = enderecoRepository.findById(idToDelete);
        List<Endereco> remainingAdresses = enderecoRepository.findAll();

        assertThat(deletedAdress).isEmpty();
        assertThat(remainingAdresses).hasSize(1);


    }

    @Test
    @DisplayName("Should update city in address ")
    void shouldUpdateCityAddress() {
        Endereco savedAdress = enderecoRepository.save(endereco1);

        savedAdress.setCidade("Minas Gerais");
        Endereco updateAdress = enderecoRepository.save(endereco1);

        assertThat(updateAdress).isNotNull();
        assertThat(updateAdress.getId()).isEqualTo(savedAdress.getId());
        assertThat(updateAdress.getCidade()).isEqualTo("Minas Gerais");


    }
}
