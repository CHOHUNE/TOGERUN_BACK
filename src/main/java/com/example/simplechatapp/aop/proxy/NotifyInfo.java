package com.example.simplechatapp.aop.proxy;

import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.User;

import java.util.Set;

public interface NotifyInfo {
    Set<User> getReceiver();
    Long getGoUrlId();
    NotificationType getNotificationType();

}
