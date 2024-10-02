package com.example.demo.models;

import com.example.demo.annotations.ValidCapacity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "class_room")
@Data
public class ClassRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ValidCapacity
    @Column(nullable = false)
    private Integer capacity;
}
