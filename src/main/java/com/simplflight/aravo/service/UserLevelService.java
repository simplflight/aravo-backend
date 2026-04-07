package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.event.UserLeveledUpEvent;
import com.simplflight.aravo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLevelService {

    private final UserRepository userRepository;

    // 20 fragmentos x Nível alcançado.
    private static final Integer SHARDS_PER_LEVEL = 20;

    /**
     * Distribui as recompensas quando um usuário sobe de nível.
     */
    @EventListener
    public void handleUserLeveledUp(UserLeveledUpEvent event) {
        User user = event.user();

        int rewardShards = user.getLevel() * SHARDS_PER_LEVEL;

        user.addShards(rewardShards);

        userRepository.save(user);
    }
}
