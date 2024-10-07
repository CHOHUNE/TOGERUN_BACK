package com.example.simplechatapp.service.scheduler;

import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class UserCleanScheduler {

    private final UserRepository userRepository;

}
