package com.vr.autorizador.exception;

import com.vr.autorizador.enums.RegraAutorizacaoEnum;
import lombok.Getter;

@Getter
public class TransacaoInvalidaException extends RuntimeException {
    private final RegraAutorizacaoEnum motivo;

    public TransacaoInvalidaException(RegraAutorizacaoEnum motivo) {
        super(motivo.name());
        this.motivo = motivo;
    }
}
