package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "item")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    @Column(name = "owner_id")
    private Long ownerId;
}