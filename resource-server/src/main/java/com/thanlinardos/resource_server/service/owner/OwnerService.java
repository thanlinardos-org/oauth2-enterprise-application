package com.thanlinardos.resource_server.service.owner;

import com.thanlinardos.resource_server.model.constants.SecurityConstants;
import com.thanlinardos.resource_server.model.entity.owner.OwnerJpa;
import com.thanlinardos.resource_server.model.entity.role.RoleJpa;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.repository.api.OwnerRepository;
import com.thanlinardos.resource_server.service.role.RoleServiceImpl;
import com.thanlinardos.spring_enterprise_library.error.errorcodes.ErrorCode;
import com.thanlinardos.spring_enterprise_library.parse.utils.ParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final RoleServiceImpl roleService;

    public OwnerJpa getOwnerByUsername(String username) {
        return ownerRepository.getFirstByCustomerUsername(username)
                .orElseThrow(() -> ErrorCode.NONE_FOUND.createCoreException("Owner not found by username: {0}", new Object[]{username}));
    }

    @Transactional
    public OwnerModel save(OwnerModel owner) {
        OwnerJpa entity = getUpdatedEntity(owner);
        return saveOwner(owner, entity);
    }

    @Transactional
    public OwnerModel saveIfNotExistsByUuid(OwnerModel owner) {
        if (ownerRepository.existsByUuid(owner.getUuid())) {
            return owner;
        } else {
            OwnerJpa entity = owner.toEntity();
            return saveOwner(owner, entity);
        }
    }

    @Transactional
    public OwnerModel saveGuest(OwnerModel owner) {
        OwnerJpa entity = owner.toEntity();
        List<RoleJpa> defaultGuestRoles = findDefaultGuestRoles();
        entity.setRoles(defaultGuestRoles);
        entity.setPrivilegeLevel(getDefaultGuestPrivilegeLvl(defaultGuestRoles));
        return saveOwner(owner, entity);
    }

    private OwnerJpa getUpdatedEntity(OwnerModel owner) {
        OwnerJpa entity = owner.toEntity();
        ownerRepository.getFirstByUuid(owner.getUuid())
                .ifPresent(existing -> setRelatedIds(existing, entity));
        return entity;
    }

    private void setRelatedIds(OwnerJpa existing, OwnerJpa updated) {
        updated.setId(existing.getId());
        if (existing.getCustomer() != null) {
            updated.setCustomerId(existing.getCustomer().getId());
        } else if (existing.getClient() != null) {
            updated.setClientId(existing.getClient().getId());
        }
    }

    private OwnerModel saveOwner(OwnerModel owner, OwnerJpa entity) {
        entity = ownerRepository.save(entity);
        owner.setId(entity.getId());
        return owner;
    }

    private @Nonnull Integer getDefaultGuestPrivilegeLvl(List<RoleJpa> defaultGuestRoles) {
        return defaultGuestRoles.stream()
                .map(RoleJpa::getPrivilegeLvl)
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);
    }

    @Transactional
    public Optional<Integer> delete(UUID uuid) {
        return ownerRepository.getFirstByUuid(uuid)
                .map(this::deleteCascade);
    }

    private int deleteCascade(OwnerJpa ownerJpa) {
        ownerRepository.deleteCascade(ownerJpa);
        return 1;
    }

    private List<RoleJpa> findDefaultGuestRoles() {
        return roleService.findRoles(SecurityConstants.DEFAULT_GUEST_ROLES).stream()
                .map(RoleModel::toEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<OwnerModel> getOwnerByPrincipalName(String name) {
        return ParserUtil.safeParseOptionalUUID(name)
                .flatMap(this::getOwnerByServiceAccountId)
                .or(() -> getOwnerByName(name));
    }

    @Transactional(readOnly = true)
    public Optional<OwnerModel> getOwnerByServiceAccountId(UUID uuid) {
        return ownerRepository.getFirstByClientServiceAccountId(uuid)
                .map(OwnerModel::new);
    }

    public Optional<OwnerModel> getOwnerByName(String name) {
        return ownerRepository.getFirstByName(name)
                .map(OwnerModel::new);
    }

    public Optional<OwnerModel> getOwnerByUuid(UUID uuid) {
        return ownerRepository.getFirstByUuid(uuid)
                .map(OwnerModel::new);
    }

    public boolean ownerExistsByClientServiceAccountId(String name) {
        return ParserUtil.safeParseOptionalUUID(name)
                .map(ownerRepository::existsByClientServiceAccountId)
                .orElse(false);
    }

    public boolean ownerExistsByName(String name) {
        return ownerRepository.existsByName(name);
    }

    public boolean ownerExistsByUuid(UUID uuid) {
        return ownerRepository.existsByUuid(uuid);
    }
}
