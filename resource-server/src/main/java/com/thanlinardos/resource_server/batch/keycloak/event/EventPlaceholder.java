package com.thanlinardos.resource_server.batch.keycloak.event;

import com.thanlinardos.resource_server.model.info.TaskType;
import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import com.thanlinardos.spring_enterprise_library.objects.utils.CollectionUtils;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.thanlinardos.spring_enterprise_library.objects.utils.PredicateUtils.isEqualTo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class EventPlaceholder<T extends BasicIdJpa> extends BasicIdModel<T, EventPlaceholder<T>> {

    private UUID uuid;
    private long time;
    private EventStatusType status;
    private UUID realmId;
    private String error;

    protected EventPlaceholder(UUID uuid, long id, long time, EventStatusType status, UUID realmId, String error) {
        super(id);
        this.uuid = uuid;
        this.time = time;
        this.status = status;
        this.realmId = realmId;
        this.error = error;
    }

    public boolean isFailed() {
        return status.isFailed();
    }

    public boolean isNotSkippedAsFailed() {
        return !isSkippedAsFailed();
    }

    public boolean isNotIgnored() {
        return !isIgnored();
    }

    private boolean isIgnored() {
        return EventStatusType.IGNORED.equals(status);
    }

    private boolean isSkippedAsFailed() {
        return EventStatusType.SKIPPED_AS_FAILED.equals(status);
    }

    @Nullable
    public abstract UUID getResourceId();

    public TaskType getTaskType() {
        return TaskType.KEYCLOAK_EVENT_TASK;
    }

    /**
     * Checks if none of the given events match the resourceId of this {@link EventPlaceholder}, or if the resourceId is null.
     *
     * @param events the given {@link EventPlaceholder}s to match against.
     * @param <E>    the type of {@link EventPlaceholder}.
     * @return true if none of the given events match the resourceId of this {@link EventPlaceholder} or if the resourceId is null, otherwise false.
     */
    public <E extends EventPlaceholder<T>> boolean noneMatchingResourceIdOrIsNull(List<E> events) {
        return Optional.ofNullable(getResourceId())
                .map(id -> noneMatchingResourceId(events))
                .orElse(true);
    }

    public <E extends EventPlaceholder<T>> boolean noneMatchingResourceId(List<E> events) {
        return events.stream()
                .noneMatch(isEqualTo(getResourceId(), E::getResourceId));
    }

    /**
     * Checks if this event is contained in the given list of {@link EventPlaceholder}s, by checking its UUID.
     *
     * @param events the given {@link EventPlaceholder}s.
     * @param <E>    the type of {@link EventPlaceholder}.
     * @return true if this event is contained in the given list of {@link EventPlaceholder}s, otherwise false.
     */
    public <E extends EventPlaceholder<T>> boolean isContainedInEvents(List<E> events) {
        return CollectionUtils.contains(events, isEqualTo(getUuid(), EventPlaceholder::getUuid));
    }
}
