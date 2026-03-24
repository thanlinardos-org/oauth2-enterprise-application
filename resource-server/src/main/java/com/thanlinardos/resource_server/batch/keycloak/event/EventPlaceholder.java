package com.thanlinardos.resource_server.batch.keycloak.event;

import com.thanlinardos.resource_server.model.info.TaskType;
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
public abstract class EventPlaceholder extends BasicIdModel {

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
     * @param <T>    the type of {@link EventPlaceholder}.
     * @return true if none of the given events match the resourceId of this {@link EventPlaceholder} or if the resourceId is null, otherwise false.
     */
    public <T extends EventPlaceholder> boolean noneMatchingResourceIdOrIsNull(List<T> events) {
        return Optional.ofNullable(getResourceId())
                .map(id -> noneMatchingResourceId(events))
                .orElse(true);
    }

    public <T extends EventPlaceholder> boolean noneMatchingResourceId(List<T> events) {
        return events.stream()
                .noneMatch(isEqualTo(getResourceId(), T::getResourceId));
    }

    /**
     * Checks if this event is contained in the given list of {@link EventPlaceholder}s, by checking its UUID.
     *
     * @param events the given {@link EventPlaceholder}s.
     * @param <T>    the type of {@link EventPlaceholder}.
     * @return true if this event is contained in the given list of {@link EventPlaceholder}s, otherwise false.
     */
    public <T extends EventPlaceholder> boolean isContainedInEvents(List<T> events) {
        return CollectionUtils.contains(events, isEqualTo(getUuid(), EventPlaceholder::getUuid));
    }
}
