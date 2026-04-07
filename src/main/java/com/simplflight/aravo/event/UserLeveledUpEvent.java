package com.simplflight.aravo.event;

import com.simplflight.aravo.domain.entity.User;

/**
 * Evento disparado quando o usuário sobe de nível.
 */
public record UserLeveledUpEvent(User user) {
}
