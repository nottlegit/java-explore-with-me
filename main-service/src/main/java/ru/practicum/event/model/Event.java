package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.category.model.Category;
import ru.practicum.common.enums.EventState;
import ru.practicum.location.model.Location;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "events")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String annotation;

    @OneToOne
    private Category category;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @OneToOne
    private User initiator;

    @OneToOne(cascade = {CascadeType.ALL})
    private Location location;

    private Boolean paid;

    @Column(name = "participant_limit")
    private int participantLimit;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_state")
    private EventState EventState;

    private String title;
}
