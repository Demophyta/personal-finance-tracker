package com.finance.tracker.service;

import com.finance.tracker.model.Notification;
import com.finance.tracker.model.User;

import java.util.List;

public interface NotificationService {

    void createNotification(User user, String message);

    List<Notification> getUnreadNotifications(User user);

    void markAsRead(Long id);

    void sendBudgetAlert(User user, String message);

    void sendRecurringTransactionReminder(User user, String message);

    void sendGeneralReminder(User user, String message);
    List<Notification> getAllNotifications(User user);

}
