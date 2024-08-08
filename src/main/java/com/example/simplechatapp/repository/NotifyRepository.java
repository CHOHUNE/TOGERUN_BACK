package com.example.simplechatapp.repository;

import com.example.simplechatapp.entity.Notify;
import com.example.simplechatapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotifyRepository extends JpaRepository<Notify, Long>{

    Page<Notify> findByReceiverOrderByCreatedAtDesc(User user, Pageable pageable);

}
