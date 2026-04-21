package com.thanlinardos.resource_server;

import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaExtendedRepositoryImpl;
import com.thanlinardos.resource_server.service.role.RoleCacheService;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Collection;
import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableJpaRepositories(repositoryBaseClass = BasicIdJpaExtendedRepositoryImpl.class)
@RequiredArgsConstructor
@Slf4j
public class ResourceServerApplication {

	private final RoleCacheService roleCacheService;

    public static void main(String[] args) {
		SpringApplication.run(ResourceServerApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> onStartUp();
	}

	private void onStartUp() {
		Collection<RoleModel> roles = roleCacheService.getAllRoles();
		List<Authority> authorities = roleCacheService.getAllAuthorities();
		log.info("Loaded {} roles and {} authorities", roles.size(), authorities.size());
	}
}
