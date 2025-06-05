package ru.practicum.shareit.request;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "items_request")
@Builder
@Data
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "requestor_id")
    private User requestor;

}
