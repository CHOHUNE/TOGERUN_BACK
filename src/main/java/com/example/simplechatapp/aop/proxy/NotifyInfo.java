package com.example.simplechatapp.aop.proxy;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.NotificationType;
import com.example.simplechatapp.entity.NotifyMessage;

import java.util.Set;

public interface NotifyInfo {
    Set<String> getReceiver();
    String getGoUrlId();
    NotificationType getNotificationType();
    NotifyMessage getNotifyMessage();

}
