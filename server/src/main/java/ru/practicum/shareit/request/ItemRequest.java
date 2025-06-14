package ru.practicum.shareit.request;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "items_request")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
