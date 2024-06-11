package com.example.simplechatapp.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

}
