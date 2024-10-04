package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Notify;
import com.example.simplechatapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotifyRepository extends JpaRepository<Notify, Long>{

    Page<Notify> findByReceiverOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notify n WHERE n.receiver = :user AND n.isRead = false")
    int countUnreadNotifications(User user);

    @Query("SELECT n FROM Notify n WHERE n.receiver = :receiver AND n.isRead = false")
    List<Notify> findAllByReceiverAndIsReadFalse(User receiver);

}
