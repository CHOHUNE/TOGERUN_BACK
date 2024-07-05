package com.example.simplechatapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "exercise_events")
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private User organizer;

    @OneToMany(mappedBy = "exerciseEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoom> chatRooms = new ArrayList<>();
}
