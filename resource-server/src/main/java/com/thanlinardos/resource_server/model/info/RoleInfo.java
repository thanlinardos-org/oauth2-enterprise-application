package com.thanlinardos.resource_server.model.info;

import java.util.Set;

public record RoleInfo(String name, int privilegeLvl, Set<String> authorities) {
}
