package com.ibm.ai.workbench.api.workspaces.dao;

import com.datastax.astra.spring.DataApiTableCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class WorkspaceRepository extends DataApiTableCrudRepository<Workspace, UUID> {
}
