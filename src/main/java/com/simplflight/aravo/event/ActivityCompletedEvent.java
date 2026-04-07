package com.simplflight.aravo.event;

import com.simplflight.aravo.domain.entity.Activity;

/**
 * Evento disparado quando um usuário conclui uma atividade.
 * Carrega a atividade recém-salva.
 */
public record ActivityCompletedEvent(Activity activity) {
}