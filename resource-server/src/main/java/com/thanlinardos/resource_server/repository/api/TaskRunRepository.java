package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.task.TaskRunJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

public interface TaskRunRepository extends BasicIdJpaRepository<TaskRunJpa>, CustomTaskRunRepository {
}
