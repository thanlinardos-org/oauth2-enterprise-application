package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.contact.NoticeDetailsJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

public interface NoticeRepository extends BasicIdJpaRepository<NoticeDetailsJpa>, CustomNoticeRepository {
}
