package com.example.simplechatapp.aop.proxy;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.NotificationType;

import java.util.Set;

public interface NotifyInfo {
    Set<String> getReceiver();
    Long getGoUrlId();
    NotificationType getNotificationType();

}
