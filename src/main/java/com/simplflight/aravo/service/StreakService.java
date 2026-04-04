package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.domain.entity.UserDailyTracking;
import com.simplflight.aravo.domain.enums.DailyTrackingStatus;
import com.simplflight.aravo.domain.enums.ItemType;
import com.simplflight.aravo.dto.response.StreakCalendarResponse;
import com.simplflight.aravo.repository.InventoryRepository;
import com.simplflight.aravo.repository.UserDailyTrackingRepository;
import com.simplflight.aravo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Ofensiva (Streak).
 * Utiliza o padrão arquitetural "Lazy Evaluation".
 * Em vez de rodar tarefas de agendamento (Cron Jobs) à meia-noite para quebrar
 * as ofensivas dos usuários, o sistema avalia a ofensiva do usuário de forma reativa,
 * apenas no momento em que ele interage com o sistema (registro de Atividade ou abertura de Calendário).
 */
@Service
@RequiredArgsConstructor
public class StreakService {

    private final UserDailyTrackingRepository trackingRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    /**
     * Retorna o histórico de atividades mensais do usuário para renderizar o calendário.
     * Antes de buscar, força uma reavaliação da ofensiva para garantir que dias perdidos
     * sejam preenchidos com gelo (STREAK_FREEZE) ou que a ofensiva seja zerada.
     */
    @Transactional
    public StreakCalendarResponse getCalendar(User user, int month, int year) {

        // 1. Chama o motor Lazy para processar dias inativos
        evaluatePastStreak(user, LocalDate.now());

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        // 2. Busca o histórico do mês
        List<UserDailyTracking> trackings = trackingRepository
                .findByUserAndTrackingDateBetweenOrderByTrackingDateAsc(user, start, end);

        List<StreakCalendarResponse.DailyRecord> history = trackings.stream()
                .map(t -> new StreakCalendarResponse.DailyRecord(
                        t.getTrackingDate().toString(),
                        t.getActivitiesCount(),
                        t.getStatus().name()
                )).toList();

        return new StreakCalendarResponse(
                user.getStreak(),
                user.getHighestStreak(),
                history
        );
    }

    /**
     * Registra que o usuário teve uma interação bem-sucedida.
     * Adiciona a prática ao dia no calendário e gerencia a manutenção/incremento da ofensiva.
     */
    @Transactional
    public void recordActivityToday(User user, LocalDate today) {

        // 1. Processa a sequência do usuário antes de registrar o dia atual
        evaluatePastStreak(user, today);

        // 2. Busca ou cria o registro diário de ofensiva
        UserDailyTracking tracking = trackingRepository.findByUserAndTrackingDate(user, today)
                .orElse(UserDailyTracking.builder()
                        .user(user)
                        .trackingDate(today)
                        .activitiesCount(0)
                        .status(DailyTrackingStatus.COMPLETED)
                        .build());

        tracking.setActivitiesCount(tracking.getActivitiesCount() + 1);

        trackingRepository.save(tracking);

        // 3. Atualiza os metadados de ofensiva se for o primeiro foco do dia
        if (!today.equals(user.getLastActivityDate())) {
            user.setStreak(user.getStreak() + 1);
            user.setLastActivityDate(today);

            if (user.getStreak() > user.getHighestStreak()) {
                user.setHighestStreak(user.getStreak());
            }

            userRepository.save(user);
        }
    }

    /**
     * O núcleo da "Lazy Evaluation".
     * Verifica a lacuna (dias) entre a última atividade do usuário e a data de hoje.
     * Tenta salvar a ofensiva usando itens do inventário; caso não haja itens, a ofensiva é quebrada.
     */
    @Transactional
    public void evaluatePastStreak(User user, LocalDate today) {

        LocalDate lastActivity = user.getLastActivityDate();

        // Se é a primeira vez que o usuário usa o app, não há o que avaliar
        if (lastActivity == null) return;

        // Subtrai 1 porque o dia atual não é contabilizado como "perdido"
        long missedDays = ChronoUnit.DAYS.between(lastActivity, today) - 1;

        // Se ele está em dia com a ofensiva, ou se já estava zerada, nada é processado
        if (missedDays <= 0 || user.getStreak() == 0) return;

        // Itera sobre o número de dias perdidos cronologicamente
        for (int i = 1; i <= missedDays; i++) {
            LocalDate missedDate = lastActivity.plusDays(i);

            boolean recovered = tryConsumeStreakFreeze(user);

            if (recovered) {
                // Registra o dia no calendário como FROZEN
                UserDailyTracking freezeTrack = UserDailyTracking.builder()
                        .user(user)
                        .trackingDate(missedDate)
                        .activitiesCount(0)
                        .status(DailyTrackingStatus.FROZEN)
                        .build();

                trackingRepository.save(freezeTrack);
            } else {
                // Os itens acabaram. Ofensiva zerada.
                user.setStreak(0);

                userRepository.save(user);

                break; // Interrompe o loop (dias restantes não são marcados)
            }
        }

        // Se ele sobreviveu à lacuna, avança a última data de atividade para ontem,
        // garantindo que não processaremos os mesmos "buracos" na próxima requisição.
        if (user.getStreak() > 0) {
            user.setLastActivityDate(today.minusDays(1));

            userRepository.save(user);
        }
    }

    /**
     * Busca um item de bloqueio de ofensiva e tenta reduzir 1 unidade do inventário do usuário.
     * Retorna true se o tem foi consumido, ou false se o inventário estiver zerado.
     */
    private boolean tryConsumeStreakFreeze(User user) {
        return inventoryRepository.findByUserAndItem_TypeAndQuantityGreaterThan(user, ItemType.STREAK_FREEZE, 0)
                .map(inv -> {
                    inv.setQuantity(inv.getQuantity() - 1);
                    inventoryRepository.save(inv);
                    return true;
                }).orElse(false);
    }
}