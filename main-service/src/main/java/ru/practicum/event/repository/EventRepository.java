package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByInitiator_IdOrderByCreatedOnDesc(Long initiatorId, Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE (:users IS NULL OR e.initiator.id IN :users)
          AND (:states IS NULL OR e.state IN :states)
          AND (:categories IS NULL OR e.category.id IN :categories)
          AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)
          AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
    """)
    Page<Event> findEvents(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    @Query("""
        SELECT e FROM Event e
        LEFT JOIN FETCH e.category
        WHERE e.state = 'PUBLISHED'
          AND (:text IS NULL 
               OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))
          AND (:categories IS NULL OR e.category.id IN :categories)
          AND (:paid IS NULL OR e.paid = :paid)
          AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)
          AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
          AND (
                :onlyAvailable = FALSE
                OR e.participantLimit = 0
                OR (
                    SELECT COUNT(r.id)
                    FROM ParticipationRequest r
                    WHERE r.event.id = e.id
                      AND r.status = ru.practicum.request.enums.ParticipationRequestStatus.CONFIRMED
                ) < e.participantLimit
          )
    """)
    Page<Event> findPublicEvents(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            Pageable pageable
    );

    boolean existsByCategory_Id(Long id);
}
