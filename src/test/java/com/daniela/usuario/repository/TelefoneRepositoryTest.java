package com.daniela.usuario.repository;

import com.daniela.usuario.infrastructure.entity.Telefone;
import com.daniela.usuario.infrastructure.repository.TelefoneRepository;
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
public class TelefoneRepositoryTest {

    @Autowired
    TelefoneRepository telefoneRepository;

    Telefone telefone1;
    Telefone telefone2;

    @BeforeEach
    void setup() {

        telefone1 = new Telefone(null, "123456789", "11", null);

        telefone2 = new Telefone(null, "987654321", "12", null);

    }

    @Test
    @DisplayName("Should save new phone number")
    void shouldSavePhoneNumberSucess() {
        Telefone savedNumber = telefoneRepository.save(telefone1);

        assertThat(savedNumber).isNotNull();
        assertThat(savedNumber.getId()).isGreaterThan(0L);
        assertThat(savedNumber.getId()).isNotNull();

    }

    @Test
    @DisplayName("Should save multiples phone numbers")
    void shouldSaveMultiplesPhoneNumbers() {
        List<Telefone> telefones = List.of(telefone1, telefone2);
        List<Telefone> savedNumbers = telefoneRepository.saveAll(telefones);

        assertThat(savedNumbers).isNotNull();
        assertThat(savedNumbers).hasSize(2);
        assertThat(savedNumbers.get(0).getNumero()).isEqualTo("123456789");
        assertThat(savedNumbers.get(1).getNumero()).isEqualTo("987654321");
    }

    @Test
    @DisplayName("Should find multiples phone numbers")
    void shouldFindPhoneNumbers() {
        List<Telefone> numbers = List.of(telefone1, telefone2);
        telefoneRepository.saveAll(numbers);

        List<Telefone> foundAll = telefoneRepository.findAll();

        assertThat(foundAll).hasSize(2);
        assertThat(foundAll).isNotNull();


    }

    @Test
    @DisplayName("Should delete phone number by id")
    void shouldDeletePhoneNumberById() {
        Telefone number = telefoneRepository.save(telefone1);

        telefoneRepository.deleteById(number.getId());

        Optional<Telefone> remove = telefoneRepository.findById(number.getId());

        assertThat(remove).isEmpty();


    }

    @Test
    @DisplayName("Should delete one phone number by id")
    void shouldDeleteOnePhoneNumberById() {
        List<Telefone> numbers = List.of(telefone1, telefone2);
        List<Telefone> savedNumbers = telefoneRepository.saveAll(numbers);

        assertThat(savedNumbers).hasSize(2);

        Long idToDelete = savedNumbers.get(0).getId();
        telefoneRepository.deleteById(idToDelete);

        Optional<Telefone> remove = telefoneRepository.findById(idToDelete);
        List<Telefone> remainingAdresses = telefoneRepository.findAll();

        assertThat(remove).isEmpty();
        assertThat(remainingAdresses).hasSize(1);


    }

    @Test
    @DisplayName("Should update ddd in phone number")
    void shouldUpdateDddPhoneNumber() {
        Telefone phoneNumber = telefoneRepository.save(telefone2);
        phoneNumber.setDdd("35");

        Telefone updateNumber = telefoneRepository.save(phoneNumber);
        assertThat(updateNumber).isNotNull();
        assertThat(updateNumber.getId()).isEqualTo(phoneNumber.getId());
        assertThat(updateNumber.getDdd()).isEqualTo("35");
    }

}
