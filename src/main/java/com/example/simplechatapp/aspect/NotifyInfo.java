package com.example.simplechatapp.aspect;

import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.User;

public interface NotifyInfo {
    User getReceiver();
    Long getGoUrlId();
    NotificationType getNotificationType();

}
