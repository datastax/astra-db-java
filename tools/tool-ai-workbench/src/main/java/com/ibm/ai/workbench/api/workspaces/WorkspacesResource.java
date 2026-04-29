package com.ibm.ai.workbench.api.workspaces;

import com.datastax.astra.client.core.query.Filters;
import com.ibm.ai.workbench.api.workspaces.dao.Workspace;
import com.ibm.ai.workbench.api.workspaces.dao.WorkspaceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("api/v1/workspaces")
@Tag(name = "Books", description = "CRUD REST API for books")
public class WorkspacesResource {

    private final WorkspaceRepository workspacesRepository;

    public WorkspacesResource(WorkspaceRepository repo) {
        this.workspacesRepository = repo;
    }

    @GetMapping
    @Operation(summary = "List all workspaces")
    public List<Workspace> findAll() {
        return StreamSupport.stream(workspacesRepository.findAll().spliterator(), false).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a workspace by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workspace found"),
            @ApiResponse(responseCode = "404", description = "Workspace not found", content = @Content(schema = @Schema()))
    })
    public Workspace findById(@PathVariable UUID id) {
        return workspacesRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Worspace with id '%s' was not found".formatted(id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new book")
    public Workspace create(@RequestBody Workspace workspace) {
        validateWorkspace(workspace);
        if (workspace.getUid() == null) {
            workspace.setUid(UUID.randomUUID());
            workspace.setCreatedAt(Instant.now());
        }
        assertWorkspaceDoesNotExist(workspace.getUid());
        return workspacesRepository.save(workspace);
    }

    private void validateWorkspace(Workspace wkSpace) {
        if (wkSpace == null) {
            throw new IllegalArgumentException("Workspace payload must be provided");
        }
        if (wkSpace.getName() == null || wkSpace.getName().isBlank()) {
            throw new IllegalArgumentException("Workspace name must be provided");
        }
        if (wkSpace.getUrl() == null || wkSpace.getUrl().isBlank()) {
            throw new IllegalArgumentException("Workspace url must be provided");
        }
    }

    private void assertWorkspaceExist(UUID workspaceId) {
        if (!workspacesRepository.existsById(workspaceId)) {
            throw new NoSuchElementException("Workspace with id '%s' was not found".formatted(workspaceId));
        }
    }

    private void assertWorkspaceDoesNotExist(UUID workspaceId) {
        if (workspacesRepository.existsById(workspaceId)) {
            throw new IllegalArgumentException("Workspace with id '%s' already exists".formatted(workspaceId));
        }
    }


    @DeleteMapping("/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    @Operation(summary = "Flush all workspaces")
    public void flushBooks() {
        workspacesRepository.deleteAll();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing book")
    public Workspace update(@PathVariable UUID id, @RequestBody Workspace workspace) {
        validateWorkspace(workspace);
        assertWorkspaceExist(workspace.getUid());
        Workspace existing = workspacesRepository.findById(id).get();
        existing.setUpdatedAt(Instant.now());
        existing.setCredentials(workspace.getCredentials());
        workspacesRepository.getTable().insertOne(existing);
        return workspacesRepository.findById(id).get();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a workspace")
    public void delete(@PathVariable String id) {
        // Todo
        // delete documents, catalogs, vector store
    }


}