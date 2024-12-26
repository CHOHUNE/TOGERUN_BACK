package com.example.simplechatapp.util;


import lombok.Getter;

@Getter
public class LockException extends RuntimeException{
    private final String lockKey;

    public LockException(String message, String lockKey) {
        super(message); // 부모 클래스의 생성자 호출
        this.lockKey=lockKey; // lockKey 초기화
    }

    public static class AcquisitionException extends LockException {
        public AcquisitionException(String lockKey) {
            super("Failed to acquire lock", lockKey);
        }
    }

    public static class ReleaseException extends LockException {
        public ReleaseException(String lockKey) {
            super("Failed to release lock", lockKey);
        }
    }


}
