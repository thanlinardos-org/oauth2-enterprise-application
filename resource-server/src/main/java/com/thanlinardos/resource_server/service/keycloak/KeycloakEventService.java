package com.thanlinardos.resource_server.service.keycloak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanlinardos.resource_server.aspect.annotation.ExcludeFromLoggingAspect;
import com.thanlinardos.resource_server.batch.keycloak.event.AdminEventRepresentationPlaceholder;
import com.thanlinardos.resource_server.batch.keycloak.event.EventPlaceholder;
import com.thanlinardos.resource_server.batch.keycloak.event.EventRepresentationPlaceholder;
import com.thanlinardos.resource_server.batch.keycloak.event.EventStatusType;
import com.thanlinardos.resource_server.batch.keycloak.event.ResourceIdType;
import com.thanlinardos.resource_server.batch.keycloak.event.RoleRepresentationPlaceholder;
import com.thanlinardos.resource_server.model.entity.keycloak.KeycloakAdminEventJpa;
import com.thanlinardos.resource_server.model.entity.keycloak.KeycloakEventJpa;
import com.thanlinardos.resource_server.model.info.OwnerType;
import com.thanlinardos.resource_server.model.info.TaskType;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.repository.api.KeycloakAdminEventRepository;
import com.thanlinardos.resource_server.repository.api.KeycloakEventRepository;
import com.thanlinardos.resource_server.service.owner.OwnerService;
import com.thanlinardos.resource_server.service.role.api.OauthRoleService;
import com.thanlinardos.resource_server.service.task.TaskRunService;
import com.thanlinardos.resource_server.service.user.api.UserService;
import com.thanlinardos.spring_enterprise_library.error.errorcodes.ErrorCode;
import com.thanlinardos.spring_enterprise_library.error.exceptions.CoreException;
import com.thanlinardos.spring_enterprise_library.objects.utils.CollectionUtils;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.exception.KeycloakException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.event.Level;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RefreshScope
@Service
@RequiredArgsConstructor
public class KeycloakEventService {

    private static final String EMAIL = "email";
    private static final String FAILED_TO_PARSE_ID_FROM_RESOURCE_PATH = "Failed to parse id from resource path:";
    private static final int MAX_RESULTS = 1000;

    private final ObjectMapper objectMapper;
    private final OwnerService ownerService;
    private final UserService userService;
    private final RealmResource keycloakRealm;
    private final TaskRunService taskRunService;
    private final KeycloakEventRepository eventRepository;
    private final KeycloakAdminEventRepository adminEventRepository;
    private final OauthRoleService roleService;

    @ExcludeFromLoggingAspect
    public <T extends EventPlaceholder> List<T> fetchSortedKeycloakEvents() {
        return getSortedEvents(fetchEvents(), fetchAdminEvents());
    }

    private <T extends EventPlaceholder> List<T> getSortedEvents(Stream<EventRepresentationPlaceholder> events, Stream<AdminEventRepresentationPlaceholder> adminEvents) {
        return Stream.concat(((Stream<T>) events), ((Stream<T>) adminEvents))
                .sorted(Comparator.comparingLong(EventPlaceholder::getTime))
                .toList();
    }

    private Stream<EventRepresentationPlaceholder> fetchEvents() {
        String dateFrom = String.valueOf(getLastEventTime() + 1);
        return keycloakRealm.getEvents(null, null, null, dateFrom, null, null, null, MAX_RESULTS).stream()
                .map(EventRepresentationPlaceholder::new);
    }

    private Stream<AdminEventRepresentationPlaceholder> fetchAdminEvents() {
        String dateFrom = String.valueOf(getLastEventTime() + 1);
        return keycloakRealm.getAdminEvents(null, null, null, null, null, null, null, dateFrom, null, null, MAX_RESULTS).stream()
                .map(AdminEventRepresentationPlaceholder::new);
    }

    private <T extends EventPlaceholder> boolean shouldProcessKeycloakEvent(T event) {
        return switch (event) {
            case AdminEventRepresentationPlaceholder adminEvent -> shouldProcessAdminEvent(adminEvent);
            case EventRepresentationPlaceholder eventPlaceholder -> shouldProcessEvent(eventPlaceholder);
            default -> throw invalidEventClassException(event);
        };
    }

    private <T extends EventPlaceholder> CoreException invalidEventClassException(T event) {
        return ErrorCode.INVALID_EVENT_CLASS_INSTANCE.createCoreException("An event of unknown class type was provided: {0}", new Object[]{event.getClass().getName()});
    }

    private boolean shouldProcessEvent(EventRepresentationPlaceholder e) {
        return e.getType().shouldProcess() && (isNotProcessed(e) || e.isFailed());
    }

    private boolean shouldProcessAdminEvent(AdminEventRepresentationPlaceholder e) {
        return isNotProcessed(e) || e.isFailed();
    }

    private Set<RoleRepresentationPlaceholder> parseRoleRepresentations(String representation) {
        try {
            return ((List<RoleRepresentation>) objectMapper.readerForListOf(RoleRepresentation.class).readValue(representation)).stream()
                    .map(RoleRepresentationPlaceholder::fromRepresentation)
                    .collect(Collectors.toSet());
        } catch (JsonProcessingException e) {
            throw ErrorCode.ILLEGAL_ARGUMENT.createCoreException("Failed to parse roles from event", e);
        }
    }

    private <T extends EventPlaceholder> boolean isNotProcessed(T e) {
        return e.getTime() > getLastEventTime();
    }

    private long getLastEventTime() {
        return taskRunService.getTaskRunTime(TaskType.KEYCLOAK_EVENT_TASK);
    }

    public <T extends EventPlaceholder> void saveEventIfFailed(T event) {
        if (event.isFailed()) {
            saveEvent(event);
        }
    }

    private <T extends EventPlaceholder> void saveEvent(T event) {
        switch (event) {
            case AdminEventRepresentationPlaceholder adminEvent ->
                    adminEventRepository.save(KeycloakAdminEventJpa.fromModel(adminEvent));
            case EventRepresentationPlaceholder eventPlaceholder ->
                    eventRepository.save(KeycloakEventJpa.fromModel(eventPlaceholder));
            default -> throw invalidEventClassException(event);
        }
    }

    private <T extends EventPlaceholder> List<T> getSortedKeycloakFailedEvents() {
        return getSortedEvents(getFailedEvents(), getFailedAdminEvents());
    }

    private Stream<EventRepresentationPlaceholder> getFailedEvents() {
        return eventRepository.findAllByStatusIn(EventStatusType.getFailedStatuses()).stream()
                .map(EventRepresentationPlaceholder::new);
    }

    private Stream<AdminEventRepresentationPlaceholder> getFailedAdminEvents() {
        return adminEventRepository.findAllByStatusIn(EventStatusType.getFailedStatuses()).stream()
                .map(AdminEventRepresentationPlaceholder::new);
    }

    private <T extends EventPlaceholder> void updateEventToProcessed(T event) {
        switch (event) {
            case AdminEventRepresentationPlaceholder adminEvent ->
                    adminEventRepository.updateEventToProcessed(adminEvent.getId());
            case EventRepresentationPlaceholder eventPlaceholder ->
                    eventRepository.updateEventToProcessed(eventPlaceholder.getId());
            default -> throw invalidEventClassException(event);
        }
        event.setStatus(EventStatusType.PROCESSED);
    }

    @ExcludeFromLoggingAspect
    public <T extends EventPlaceholder> List<T> processFailedEvents() {
        List<T> failedEvents = getSortedKeycloakFailedEvents();
        processEvents(failedEvents, Collections.emptyList());
        return getFailedEvents(failedEvents);
    }

    private <T extends EventPlaceholder> List<T> getFailedEvents(List<T> events) {
        return events.stream()
                .filter(EventPlaceholder::isFailed)
                .toList();
    }

    @ExcludeFromLoggingAspect
    public <T extends EventPlaceholder> void processEvents(List<T> sortedEvents, List<T> failedEvents) {
        T latestEvent = null;
        for (T event : sortedEvents) {
            if (!skipIfHasMatchingFailedEvent(failedEvents, event)) {
                latestEvent = processEvent(event, latestEvent);
            }
        }
        updateTaskRunTime(latestEvent);
    }

    private <T extends EventPlaceholder> void updateTaskRunTime(@Nullable T event) {
        if (event != null) {
            taskRunService.updateTaskRunTime(event.getTaskType(), event.getTime());
        }
    }

    private <T extends EventPlaceholder> T processEvent(T event, T latestEvent) {
        handleKeycloakEventOrIgnore(event);
        saveEventIfFailed(event);

        if (isNotFailedAndNewerThanLatestEvent(event, latestEvent)) {
            return event;
        } else {
            return latestEvent;
        }
    }

    private <T extends EventPlaceholder> boolean isNotFailedAndNewerThanLatestEvent(T event, T latestEvent) {
        return !event.isFailed() && isNewerThanLatestEvent(event, latestEvent);
    }

    private <T extends EventPlaceholder> boolean isNewerThanLatestEvent(T event, @Nullable T latestEvent) {
        return latestEvent == null || latestEvent.getTime() < event.getTime();
    }

    private <T extends EventPlaceholder> boolean skipIfHasMatchingFailedEvent(List<T> failedEvents, T event) {
        if (event.noneMatchingResourceIdOrIsNull(failedEvents)) {
            return false;
        } else {
            return ignoreIfAlreadyFailedOrSaveAsFailed(failedEvents, event);
        }
    }

    private <T extends EventPlaceholder> boolean ignoreIfAlreadyFailedOrSaveAsFailed(List<T> failedEvents, T event) {
        if (event.isContainedInEvents(failedEvents)) {
            logKeycloakEvent(Level.ERROR, event, "Ignored event due to the same existing failed event with uuid", event.getUuid());
            event.setStatus(EventStatusType.IGNORED);
            return false;
        } else if (event.isNotSkippedAsFailed()) {
            logKeycloakEvent(Level.ERROR, event, "Skipped and saved as failed event due to existing failed event with matching resource id", event.getResourceId());
            event.setStatus(EventStatusType.SKIPPED_AS_FAILED);
            saveEventIfFailed(event);
            return true;
        } else { // event already skipped as failed
            return true;
        }
    }

    private <T extends EventPlaceholder> void handleKeycloakEventOrIgnore(T event) {
        if (event.isNotIgnored() && shouldProcessKeycloakEvent(event)) {
            switch (event) {
                case AdminEventRepresentationPlaceholder adminEvent -> tryHandleAdminEvent(adminEvent);
                case EventRepresentationPlaceholder eventPlaceholder -> tryHandleEvent(eventPlaceholder);
                default -> throw invalidEventClassException(event);
            }
        } else {
            event.setStatus(EventStatusType.IGNORED);
        }
    }

    private void tryHandleEvent(EventRepresentationPlaceholder event) {
        log.trace("Keycloak event: {}", event);
        try {
            handleEvent(event);
            updateEventStatusAfterProcessing(event);
        } catch (Exception e) {
            event.setStatus(EventStatusType.FAILED);
            logEvent(Level.ERROR, event, "Failed to handle event with error", e);
        }
    }

    private <T extends EventPlaceholder> void updateEventStatusAfterProcessing(T event) {
        if (event.getStatus().equals(EventStatusType.RECEIVED)) {
            event.setStatus(EventStatusType.PROCESSED);
        } else if (event.getId() != null && event.isFailed()) {
            updateEventToProcessed(event);
        }
    }

    private void handleEvent(EventRepresentationPlaceholder event) {
        switch (event.getType()) {
            case REGISTER -> getGuestOwnerByEmailAndPersist(event);
            case UPDATE_TOTP, SEND_VERIFY_EMAIL ->
                    userService.getOwnerByIdAndPersistOrUpdate(Objects.requireNonNull(event.getUserId()), OwnerType.CUSTOMER);
            default -> log.trace("Unhandled event: {}", event);
        }
    }

    private void getGuestOwnerByEmailAndPersist(EventRepresentationPlaceholder event) {
        String email = event.getDetails().get(EMAIL);
        if (ownerService.ownerExistsByName(email)) {
            logEvent(Level.WARN, event, "Failed to register guest user. A user already exists with email", email);
            event.setStatus(EventStatusType.IGNORED);
        } else {
            userService.getGuestOwnerByEmailAndPersist(email);
        }
    }

    private void tryHandleAdminEvent(AdminEventRepresentationPlaceholder event) {
        log.trace("Keycloak admin event: {}", event);
        try {
            validateEvent(event);
            switch (event.getResourceType()) {
                case REALM_ROLE_MAPPING -> handleRealmRoleMapping(event);
                case USER -> handleUserEvent(event);
                case CLIENT -> handleClientEvent(event);
                default -> ignoreResourceType(event);
            }
            updateEventStatusAfterProcessing(event);
        } catch (Exception e) {
            event.setStatus(EventStatusType.FAILED);
            logAdminEvent(Level.ERROR, event, "Failed to handle admin event with error:", e);
        }
    }

    private void ignoreResourceType(AdminEventRepresentationPlaceholder event) {
        logAdminEvent(Level.WARN, event, "Unhandled resource type", event.getResourceType());
        event.setStatus(EventStatusType.IGNORED);
    }

    private void validateEvent(AdminEventRepresentationPlaceholder event) throws KeycloakException {
        if (event.getResourceType() == null) {
            throw new KeycloakException("Resource type is null: " + event);
        }
        if (isNotValidResourceId(event)) {
            throw new KeycloakException(FAILED_TO_PARSE_ID_FROM_RESOURCE_PATH + event.getResourcePath());
        }
    }

    private boolean isNotValidResourceId(AdminEventRepresentationPlaceholder event) {
        return event.getResourceType().hasResourceId() && event.getResourceIdType() == null || event.getResourceId() == null;
    }

    private void handleUserEvent(AdminEventRepresentationPlaceholder event) {
        UUID userId = event.getResourceId();
        switch (event.getOperationType()) {
            case DELETE -> ownerService.delete(userId)
                    .ifPresent(unused -> logAdminEvent(Level.INFO, event, "Deleted user", userId));
            case CREATE -> handleOwnerCreation(event, userId, OwnerType.CUSTOMER);
            case UPDATE -> handleOwnerUpdate(event, userId, OwnerType.CUSTOMER);
            case ACTION -> handleUserAction(event);
        }
    }

    private void handleUserAction(AdminEventRepresentationPlaceholder event) {
        logAdminEvent(Level.TRACE, event, "Unhandled action operation", event.getResourcePath());
        event.setStatus(EventStatusType.IGNORED);
    }

    private void handleOwnerUpdate(AdminEventRepresentationPlaceholder event, UUID ownerUuid, OwnerType ownerType) {
        OwnerModel ownerModel = userService.getOwnerByIdAndPersistOrUpdate(ownerUuid, ownerType);
        logAdminEvent(Level.INFO, event, "Updated " + lowerCaseName(ownerType), ownerModel);
    }

    private void handleOwnerCreation(AdminEventRepresentationPlaceholder event, UUID ownerUuid, OwnerType ownerType) {
        if (!ownerService.ownerExistsByUuid(ownerUuid)) {
            OwnerModel owner = userService.getOwnerByIdAndPersistOrUpdate(ownerUuid, ownerType);
            logAdminEvent(Level.INFO, event, "Created " + lowerCaseName(ownerType), owner);
        }
    }

    private String lowerCaseName(Enum<?> type) {
        return type.name().toLowerCase();
    }

    private void handleClientEvent(AdminEventRepresentationPlaceholder event) {
        UUID clientId = event.getResourceId();
        switch (event.getOperationType()) {
            case DELETE -> ownerService.delete(clientId)
                    .ifPresent(unused -> logAdminEvent(Level.INFO, event, "Deleted client", clientId));
            case CREATE -> handleOwnerCreation(event, clientId, OwnerType.CLIENT);
            case UPDATE -> handleOwnerUpdate(event, clientId, OwnerType.CLIENT);
            default -> logAdminEvent(Level.WARN, event, "Unhandled operation type", event.getOperationType());
        }
    }

    private void handleRealmRoleMapping(AdminEventRepresentationPlaceholder event) {
        event.setRoles(parseRoleRepresentations(event.getRepresentation()));
        Set<RoleModel> roles = parseRolesFromEvent(event);
        if (roles.isEmpty()) {
            return;
        }
        logAdminEvent(Level.TRACE, event, "roles", roles);

        if (event.getResourceIdType() == ResourceIdType.USERS) {
            UUID userId = event.getResourceId();
            Optional<OwnerModel> foundOwner = ownerService.getOwnerByUuid(userId)
                    .or(() -> ownerService.getOwnerByServiceAccountId(userId));
            switch (event.getOperationType()) {
                case CREATE -> foundOwner.ifPresent(owner -> addRolesAndUpdateOwner(event, owner, roles));
                case DELETE -> foundOwner.ifPresent(owner -> deleteRolesAndUpdateOwner(event, owner, roles));
                default -> logAdminEvent(Level.WARN, event, "Unhandled operation type", event.getOperationType());
            }
        }
    }

    private void addRolesAndUpdateOwner(AdminEventRepresentationPlaceholder event, OwnerModel owner, Collection<RoleModel> roles) {
        if (new HashSet<>(owner.getRoles()).containsAll(roles)) {
            logAdminEvent(Level.WARN, event, "Owner already has roles", roles);
        } else {
            owner.setRoles(CollectionUtils.combineToSet(roles, owner.getRoles()));
            ownerService.save(owner);
            logAdminEvent(Level.INFO, event, "Assigned to owner new roles", roles);
        }
    }

    private void deleteRolesAndUpdateOwner(AdminEventRepresentationPlaceholder event, OwnerModel owner, Collection<RoleModel> roles) {
        Set<RoleModel> finalRoles = CollectionUtils.subtractToSet(owner.getRoles(), roles);
        if (finalRoles.equals(roles)) {
            logAdminEvent(Level.WARN, event, "Owner doesn't have these roles to remove", roles);
        } else {
            owner.setRoles(finalRoles);
            ownerService.save(owner);
            logAdminEvent(Level.INFO, event, "Delete from owner these roles", roles);
        }
    }

    private <T extends EventPlaceholder> void logKeycloakEvent(Level level, T event, String message, Object parsedResource) {
        switch (event) {
            case AdminEventRepresentationPlaceholder adminEvent ->
                    logAdminEvent(level, adminEvent, message, parsedResource);
            case EventRepresentationPlaceholder eventPlaceholder ->
                    logEvent(level, eventPlaceholder, message, parsedResource);
            default -> throw invalidEventClassException(event);
        }
    }

    private void logEvent(Level level, EventRepresentationPlaceholder event, String message, Object parsedResource) {
        log.atLevel(level).log("[KEYCLOAK_EVENT][{}] {}: {}", event.getType(), message, parsedResource);
    }

    private void logAdminEvent(Level level, AdminEventRepresentationPlaceholder event, String message, Object parsedResource) {
        log.atLevel(level).log("[KEYCLOAK_ADMIN_EVENT][{} {}] {}: {}", event.getOperationType(), event.getResourceType(), message, parsedResource);
    }

    public Set<RoleModel> parseRolesFromEvent(AdminEventRepresentationPlaceholder event) {
        Set<String> roleNames = getRolesNamesFromEvent(event);
        return new HashSet<>(roleService.findRoles(roleNames));
    }

    private Set<String> getRolesNamesFromEvent(AdminEventRepresentationPlaceholder event) {
        return event.getRoles().stream()
                .map(RoleRepresentationPlaceholder::getName)
                .collect(Collectors.toSet());
    }
}
