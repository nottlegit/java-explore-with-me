package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.request.enums.ParticipationRequestStatus;
import ru.practicum.request.model.ParticipationRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<ParticipationRequest> findAllByRequesterIdOrderByCreatedDesc(Long requesterId);

    List<ParticipationRequest> findAllByEventIdOrderByCreatedAsc(Long eventId);

    List<ParticipationRequest> findAllByEventIdAndStatusOrderByCreatedAsc(Long eventId,
                                                                         ParticipationRequestStatus status);

    List<ParticipationRequest> findAllByIdInAndEventId(Collection<Long> ids, Long eventId);

    long countByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    @Query("""
            SELECT r.event.id, COUNT(r.id)
            FROM ParticipationRequest r
            WHERE r.status = :status
              AND r.event.id IN :eventIds
            GROUP BY r.event.id
            """)
    List<Object[]> countByEventIdsAndStatus(@Param("eventIds") Collection<Long> eventIds,
                                            @Param("status") ParticipationRequestStatus status);
}
