package com.thanlinardos.resource_server.repository.base;

import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BasicIdJpaRepository<T extends BasicIdJpa> extends JpaRepository<T, Long>, BasicIdJpaExtendedRepository<T> {
}
