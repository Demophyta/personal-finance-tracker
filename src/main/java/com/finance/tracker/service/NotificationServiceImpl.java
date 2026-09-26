package com.finance.tracker.service;

import com.finance.tracker.model.Notification;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final BudgetService budgetService;
    private final RecurringTransactionService recurringTransactionService;

    @Override
    public void createNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .createdAt(LocalDateTime.now())
                .read(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadFalse(user);
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void sendBudgetAlert(User user, String message) {
        createNotification(user, "💰 Budget Alert: " + message);
    }

    @Override
    public void sendRecurringTransactionReminder(User user, String message) {
        createNotification(user, "🔁 Recurring Transaction Reminder: " + message);
    }

    @Override
    public void sendGeneralReminder(User user, String message) {
        createNotification(user, "🔔 Reminder: " + message);
    }

    @Override
    public List<Notification> getAllNotifications(User user) {
        return notificationRepository.findByUser(user);
    }
    /**
     * 🕐 Scheduled job that runs every morning to send automated reminders.
     */
    @Scheduled(cron = "0 0 8 * * *") // Runs daily at 8 AM
    public void sendDailyNotifications() {
        // Example logic — you can expand this
        // 1️⃣ Notify users about nearing budget limits
        // 2️⃣ Remind users of recurring transactions today

        // This could be replaced by fetching users from DB
        // and running budgetService / recurringTransactionService checks.
        System.out.println("Automated notification check triggered at " + LocalDateTime.now());
    }
}
