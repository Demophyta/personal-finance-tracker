package com.finance.tracker.repository;

import com.finance.tracker.model.Notification;
import com.finance.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);

    List<Notification> findByUserAndReadFalse(User user);

}
