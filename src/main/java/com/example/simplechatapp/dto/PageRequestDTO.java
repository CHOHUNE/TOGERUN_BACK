package com.example.simplechatapp.dto;


import lombok.Builder;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class PageRequestDTO {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;
}
