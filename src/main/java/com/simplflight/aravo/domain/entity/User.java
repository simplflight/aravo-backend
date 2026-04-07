package com.simplflight.aravo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private Integer shards = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer xp = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer streak = 0;

    @Column(name = "highest_streak", nullable = false)
    @Builder.Default
    private Integer highestStreak = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Activity> activities = new HashSet<>();

    /**
     * Adiciona Fragmentos de Foco à carteira do usuário.
     */
    public void addShards(int amount) {
        if (amount > 0) {
            this.shards += amount;
        }
    }

    /**
     * Consome Fragmentos de Foco (compras na loja).
     */
    public void deductShards(int amount) {
        if (this.shards >= amount) {
            this.shards -= amount;
        }
    }

    public void incrementStreak(LocalDate today) {
        this.streak += 1;
        this.lastActivityDate = today;
        if (this.streak > this.highestStreak) {
            this.highestStreak = this.streak;
        }
    }

    public void resetStreak() {
        this.streak = 0;
    }

    /**
     * Adiciona XP.
     * Retorna TRUE se o usuário subiu de nível.
     */
    public boolean addXp(int gainedXp) {
        if (gainedXp <= 0) return false;

        this.xp += gainedXp;

        int newLevel = calculateLevelFromXp(this.xp);

        if (newLevel > this.level) {
            this.level = newLevel;
            return true;
        }
        return false;
    }

    /**
     * Fórmula exponencial: Level = Raiz Quadrada de (XP / constante)
     */
    private int calculateLevelFromXp(int totalXp) {
        if (totalXp < 400) return 1;

        int calculatedLevel = (int) Math.sqrt(totalXp / 100.0);
        return Math.max(1, calculatedLevel);
    }
}