package com.finance.tracker.service;

import com.finance.tracker.model.Notification;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private BudgetService budgetService;

    @Mock
    private RecurringTransactionService recurringTransactionService;

    @InjectMocks
    private NotificationServiceImpl notificationService;


    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    @Test
    void shouldCreateNotification() {

        User user = new User();
        user.setUserId(1L);

        notificationService.createNotification(
                user,
                "Test notification"
        );

        verify(notificationRepository)
                .save(any(Notification.class));
    }


    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    @Test
    void shouldGetUnreadNotifications() {

        User user = new User();
        user.setUserId(1L);

        Notification notification = Notification.builder()
                .user(user)
                .message("Unread notification")
                .read(false)
                .build();

        when(notificationRepository.findByUserAndReadFalse(user))
                .thenReturn(List.of(notification));

        List<Notification> result =
                notificationService.getUnreadNotifications(user);

        assertEquals(1, result.size());

        assertEquals(
                "Unread notification",
                result.get(0).getMessage()
        );

        assertFalse(result.get(0).isRead());

        verify(notificationRepository)
                .findByUserAndReadFalse(user);
    }


    // =========================================================
    // MARK AS READ
    // =========================================================

    @Test
    void shouldMarkNotificationAsRead() {

        Long notificationId = 1L;

        Notification notification = Notification.builder()
                .message("Test notification")
                .read(false)
                .build();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead(notificationId);

        assertTrue(notification.isRead());

        verify(notificationRepository)
                .save(notification);
    }


    // =========================================================
    // MARK AS READ - NOT FOUND
    // =========================================================

    @Test
    void shouldThrowExceptionWhenNotificationNotFound() {

        Long notificationId = 99L;

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationService.markAsRead(notificationId)
        );

        assertEquals(
                "Notification not found",
                exception.getMessage()
        );

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }


    // =========================================================
    // BUDGET ALERT
    // =========================================================

    @Test
    void shouldSendBudgetAlert() {

        User user = new User();

        notificationService.sendBudgetAlert(
                user,
                "Food budget is 80% used."
        );

        verify(notificationRepository)
                .save(any(Notification.class));
    }


    // =========================================================
    // RECURRING TRANSACTION REMINDER
    // =========================================================

    @Test
    void shouldSendRecurringTransactionReminder() {

        User user = new User();

        notificationService.sendRecurringTransactionReminder(
                user,
                "Your recurring payment is due."
        );

        verify(notificationRepository)
                .save(any(Notification.class));
    }


    // =========================================================
    // GENERAL REMINDER
    // =========================================================

    @Test
    void shouldSendGeneralReminder() {

        User user = new User();

        notificationService.sendGeneralReminder(
                user,
                "Remember to review your expenses."
        );

        verify(notificationRepository)
                .save(any(Notification.class));
    }


    // =========================================================
    // GET ALL NOTIFICATIONS
    // =========================================================

    @Test
    void shouldGetAllNotifications() {

        User user = new User();

        Notification notification1 = Notification.builder()
                .user(user)
                .message("Notification 1")
                .read(false)
                .build();

        Notification notification2 = Notification.builder()
                .user(user)
                .message("Notification 2")
                .read(true)
                .build();

        when(notificationRepository.findByUser(user))
                .thenReturn(List.of(
                        notification1,
                        notification2
                ));

        List<Notification> result =
                notificationService.getAllNotifications(user);

        assertEquals(2, result.size());

        verify(notificationRepository)
                .findByUser(user);
    }
}