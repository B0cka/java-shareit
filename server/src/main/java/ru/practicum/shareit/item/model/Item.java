package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;

@Entity
@Table(name = "items")
@Data
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    @Column(name = "owner_id")
    private Long ownerId;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private ItemRequest request;
}