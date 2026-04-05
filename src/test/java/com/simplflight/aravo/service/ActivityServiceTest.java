package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.Activity;
import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.domain.enums.ActivityCategory;
import com.simplflight.aravo.domain.enums.ActivityStatus;
import com.simplflight.aravo.dto.request.ActivityCompleteRequest;
import com.simplflight.aravo.dto.request.ActivityStartRequest;
import com.simplflight.aravo.dto.response.ActivityResponse;
import com.simplflight.aravo.engine.PointCalculationEngine;
import com.simplflight.aravo.mapper.ActivityMapper;
import com.simplflight.aravo.repository.ActivityRepository;
import com.simplflight.aravo.repository.UserRepository;
import com.simplflight.aravo.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PointCalculationEngine pointEngine;
    @Mock
    private StreakService streakService;
    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private ActivityService activityService;

    private User testUser;
    private ActivityStartRequest startRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .points(100)
                .streak(5)
                .build();

        startRequest = new ActivityStartRequest(ActivityCategory.STUDY);

        lenient().when(messageUtil.get(anyString())).thenReturn("Mensagem de erro simulada.");
    }

    @Test
    @DisplayName("Start: Deve iniciar uma atividade com sucesso e salvar como IN_PROGRESS")
    void testStartActivity_Success() {
        // Arrange (Preparação)
        when(activityRepository.existsByUserAndStatus(testUser, ActivityStatus.IN_PROGRESS))
                .thenReturn(false); // Simula que o usuário não tem atividades rodando

        Activity savedActivity = Activity.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(ActivityCategory.STUDY)
                .status(ActivityStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        when(activityRepository.save(any(Activity.class))).thenReturn(savedActivity);

        ActivityResponse expectedResponse = new ActivityResponse(
                savedActivity.getId(), ActivityCategory.STUDY, ActivityStatus.IN_PROGRESS,
                savedActivity.getStartTime(), null, null
        );

        when(activityMapper.toResponse(savedActivity)).thenReturn(expectedResponse);

        // Act (Ação)
        ActivityResponse response = activityService.startActivity(testUser, startRequest);

        // Assert (Verificação)
        assertNotNull(response);
        assertEquals(ActivityStatus.IN_PROGRESS, response.status());
        assertEquals(ActivityCategory.STUDY, response.category());

        // Captura o objeto exato que foi passado para o save()
        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(activityCaptor.capture());

        Activity captured = activityCaptor.getValue();
        assertEquals(ActivityStatus.IN_PROGRESS, captured.getStatus());
        assertNotNull(captured.getStartTime());
        assertNull(captured.getEndTime()); // Garante que o fim está vazio no início!
    }

    @Test
    @DisplayName("Start: Deve lançar erro 409 (Conflict) se usuário já tiver atividade rodando")
    void testStartActivity_Conflict() {
        // Arrange
        when(activityRepository.existsByUserAndStatus(testUser, ActivityStatus.IN_PROGRESS))
                .thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            activityService.startActivity(testUser, startRequest);
        });

        assertEquals(409, exception.getStatusCode().value(), "Deve retornar código 409 Conflict");

        // Garante que não houve tentativas de salvar nada no banco
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    @DisplayName("Complete: Deve finalizar atividade, orquestrar pontos e ofensiva com sucesso")
    void testCompleteActivity_Success() {
        // Arrange
        UUID activityId = UUID.randomUUID();

        // Cria uma atividade que começou há 30 minutos
        Activity ongoingActivity = Activity.builder()
                .id(activityId)
                .user(testUser)
                .category(ActivityCategory.STUDY)
                .status(ActivityStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now().minusMinutes(30))
                .build();

        ActivityCompleteRequest request = new ActivityCompleteRequest("Estudo de Java", "Lendo documentação");

        when(activityRepository.findById(activityId)).thenReturn(java.util.Optional.of(ongoingActivity));

        // Simula que a Engine calculou 15 pontos por essa meia hora
        when(pointEngine.calculatePoints(anyInt(), eq(ActivityCategory.STUDY), any(LocalDateTime.class)))
                .thenReturn(15);

        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivityResponse expectedResponse = new ActivityResponse(
                activityId, ActivityCategory.STUDY, ActivityStatus.COMPLETED,
                ongoingActivity.getStartTime(), LocalDateTime.now(), 15
        );
        when(activityMapper.toResponse(ongoingActivity)).thenReturn(expectedResponse);

        // Guarda os pontos originais para checar posteriormente
        int pointsBefore = testUser.getPoints();

        // Act
        ActivityResponse response = activityService.completeActivity(testUser, activityId, request);

        // Assert - Efeitos no Objeto de Retorno
        assertNotNull(response);
        assertEquals(ActivityStatus.COMPLETED, response.status());

        // Assert - Efeitos na Orquestração (verifica se chamou os componentes certos)
        verify(pointEngine, times(1)).calculatePoints(anyInt(), any(), any());
        verify(streakService, times(1)).recordActivityToday(eq(testUser), any());
        verify(userRepository, times(1)).save(testUser);

        // Assert - Efeitos nas Entidades
        assertEquals(ActivityStatus.COMPLETED, ongoingActivity.getStatus(), "O status da entidade deve mudar para COMPLETED");
        assertNotNull(ongoingActivity.getEndTime(), "O horário de término não pode ser nulo");
        assertEquals("Estudo de Java", ongoingActivity.getTitle(), "Deve ter injetado o título");
        assertEquals("Lendo documentação", ongoingActivity.getDescription());
        assertEquals(15, ongoingActivity.getPointsEarned(), "Deve ter salvo os pontos ganhos na atividade");

        // Verifica se o usuário recebeu os pontos corretamente
        assertEquals(pointsBefore + 15, testUser.getPoints(), "O usuário deve ter recebido os pontos");
    }

    @Test
    @DisplayName("Complete: Deve lançar erro 400 se atividade já estiver concluída ou não estiver IN_PROGRESS")
    void testCompleteActivity_NotInProgress() {
        // Arrange
        UUID activityId = UUID.randomUUID();

        Activity completedActivity = Activity.builder()
                .id(activityId)
                .user(testUser)
                .status(ActivityStatus.COMPLETED)
                .build();

        ActivityCompleteRequest request = new ActivityCompleteRequest("Estudo", null);

        when(activityRepository.findById(activityId)).thenReturn(java.util.Optional.of(completedActivity));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            activityService.completeActivity(testUser, activityId, request);
        });

        assertEquals(400, exception.getStatusCode().value(), "Deve retornar código 400 Bad Request");

        // Garante que nada foi manipulado
        verify(pointEngine, never()).calculatePoints(anyInt(), any(), any());
        verify(streakService, never()).recordActivityToday(any(), any());
        verify(activityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Complete: Deve lançar erro 400 se tentar concluir atividade de outro usuário")
    void testCompleteActivity_WrongUser() {
        // Arrange
        UUID activityId = UUID.randomUUID();

        // Cria um usuário "Hacker"
        User hacker = User.builder().id(UUID.randomUUID()).build();

        // A atividade pertence ao testUser
        Activity ongoingActivity = Activity.builder()
                .id(activityId)
                .user(testUser)
                .status(ActivityStatus.IN_PROGRESS)
                .build();

        ActivityCompleteRequest request = new ActivityCompleteRequest("Tentativa de Hack", null);

        when(activityRepository.findById(activityId)).thenReturn(java.util.Optional.of(ongoingActivity));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            activityService.completeActivity(hacker, activityId, request);
        });

        assertEquals(400, exception.getStatusCode().value(), "Deve retornar código 400 Bad Request");
    }

    @Test
    @DisplayName("Update: Deve atualizar título e descrição com sucesso")
    void testUpdateActivity_Success() {
        // Arrange
        UUID id = UUID.randomUUID();
        Activity activity = Activity.builder().id(id).user(testUser).title("Antigo").build();
        ActivityCompleteRequest request = new ActivityCompleteRequest("Novo Titulo", "Nova Desc");

        when(activityRepository.findById(id)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(activityMapper.toResponse(any())).thenReturn(new ActivityResponse(id, null, null, null, null, null));

        // Act
        activityService.updateActivity(testUser, id, request);

        // Assert
        assertEquals("Novo Titulo", activity.getTitle());
        verify(activityRepository).save(activity);
    }

    @Test
    @DisplayName("Delete: Deve remover atividade com sucesso")
    void testDeleteActivity_Success() {
        // Arrange
        UUID id = UUID.randomUUID();
        Activity activity = Activity.builder().id(id).user(testUser).build();
        when(activityRepository.findById(id)).thenReturn(Optional.of(activity));

        // Act
        activityService.deleteActivity(testUser, id);

        // Assert
        verify(activityRepository, times(1)).delete(activity);
    }
}