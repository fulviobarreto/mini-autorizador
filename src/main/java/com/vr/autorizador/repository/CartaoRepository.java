package com.vr.autorizador.repository;

import com.vr.autorizador.model.Cartao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartaoRepository extends JpaRepository<Cartao, Long> {
    Optional<Cartao> findByNumeroCartao(String numeroCartao);

    // Garante controle de concorrência com SELECT ... FOR UPDATE
    @Query(value = "SELECT * FROM cartoes WHERE numero_cartao = :numeroCartao FOR UPDATE", nativeQuery = true)
    Optional<Cartao> findByNumeroCartaoForUpdate(@Param("numeroCartao") String numeroCartao);
}

