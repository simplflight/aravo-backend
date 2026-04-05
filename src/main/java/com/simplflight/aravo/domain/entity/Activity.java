package com.simplflight.aravo.domain.entity;

import com.simplflight.aravo.domain.enums.ActivityCategory;
import com.simplflight.aravo.domain.enums.ActivityStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private ActivityCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime date;

    public long calculateDurationInMinutes(LocalDateTime now) {
        return Duration.between(this.startTime, now).toMinutes();
    }

    public void complete(LocalDateTime now, int earnedPoints, String title, String description) {
        this.endTime = now;
        this.status = ActivityStatus.COMPLETED;
        this.pointsEarned = earnedPoints;
        this.title = title;
        this.description = description;
    }
}