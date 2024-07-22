package com.example.simplechatapp.aop.proxy;

import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.User;

public interface NotifyInfo {
    User getReceiver();
    Long getGoUrlId();
    NotificationType getNotificationType();

}
