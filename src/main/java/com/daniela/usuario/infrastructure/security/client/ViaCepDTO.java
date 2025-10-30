package com.daniela.usuario.infrastructure.security.client;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ViaCepDTO {

    private String cep;
    private String logradouro;
    private String complemento;
    private String unidade;
    private String bairro;
    private String localidade;
    private String uf;
    private String estado;
    private String regiao;
    private String ibge;
    private String gia;
    private String ddd;
    private String siafi;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ViaCepDTO dto)) return false;
        return Objects.equals(cep, dto.cep) &&
                Objects.equals(logradouro, dto.logradouro) &&
                Objects.equals(complemento, dto.complemento) &&
                Objects.equals(unidade, dto.unidade) &&
                Objects.equals(bairro, dto.bairro) &&
                Objects.equals(localidade, dto.localidade) &&
                Objects.equals(uf, dto.uf) &&
                Objects.equals(estado, dto.estado) &&
                Objects.equals(regiao, dto.regiao) &&
                Objects.equals(ibge, dto.ibge) &&
                Objects.equals(gia, dto.gia) &&
                Objects.equals(ddd, dto.ddd) &&
                Objects.equals(siafi, dto.siafi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cep, logradouro, complemento, unidade, bairro, localidade, uf, estado,
                regiao, ibge, gia, ddd, siafi);
    }

}
