-- API ?
-- ------------------------------------
--            WORKSPACES
-- ------------------------------------

-- WORKSPACE
GET    /api/v1/workspaces
POST   /api/v1/workspaces
GET    /api/v1/workspaces/{workspace-id}
PUT    /api/v1/workspaces/{workspace-id}
DELETE /api/v1/workspaces/{workspace-id}

-- CATALOG
GET    /api/v1/workspaces/{workspace-id}/catalogs
POST   /api/v1/workspaces/{workspace-id}/catalogs
GET    /api/v1/workspaces/{workspace-id}/catalogs/{catalog-id}
PUT    /api/v1/workspaces/{workspace-id}/catalogs/{catalog-id}
DELETE /api/v1/workspaces/{workspace-id}/catalogs/{catalog-id}

-- Documents
GET    /api/v1/workspaces/{workspace-id}/catalogs/{catalog-id}/documents
POST   /api/v1/workspaces/{workspace-id}/catalogs/{catalog-id}/documents
GET    /api/v1/workspaces/{workspace-id}/catalogs/{catalog-id}/documents/{doc-id}
PUT    /api/v1/workspaces/{workspace-id}/catalogs/{catalog-id}/documents/{doc-id}
DELETE /api/v1/workspaces/{workspace-id}/catalogs/{catalog-id}/documents/{doc-id}

-- Ingest
POST /api/v1/workspaces/{workspace-id}/catalogs/{catalog-id}/ingest

-- Search
POST /api/v1/workspaces/{workspace-id}/catalogs/{catalog-id}/documents/search

-- VECTOR STORE
GET  /api/v1/workspaces/{workspace-id}/vector-stores
POST /api/v1/workspaces/{workspace-id}/vector-stores
GET  /api/v1/workspaces/{workspace-id}/vector-stores/{vector-store-id}
PUT  /api/v1/workspaces/{workspace-id}/vector-stores/{vector-store-id}
DELETE /api/v1/workspaces/{workspace-id}/vector-stores/{vector-store-id}

--
-- Configuration
-- List of Workspaces to connect to
--
CREATE TABLE wb_workspaces (
    uid         UUID,
    name        text,
    url         text,
    kind        text, -- astra / hcd / open-rag
    credentials map<text, text>,
    keyspace    text,
    created_at  timestamp,
    updated_at  timestamp,
    PRIMARY KEY (uid)
);

CREATE TABLE wb_catalog_by_workspace (
    workspace      UUID,
    uid            UUID,
    name           text,
    description    text,
    vector_store   UUID,
    created_at     timestamp,
    updated_at     timestamp,
    PRIMARY KEY ((workspace), uid)
);

--
-- list of document ingested
--
CREATE TABLE wb_documents_by_catalog (
    workspace        UUID,
    catalog_uid      UUID,
    document_uid     UUID,

    -- source
    source_doc_id    text,
    source_filename  text,
    file_type        text,
    file_size        bigint,

    -- ingestion
    md5_hash         text,
    chunk_total      int,
    ingested_at      timestamp,
    updated_at       timestamp,
    status           text,
    error_message    text,

    -- metadata
    metadata         map<text, text>,

    PRIMARY KEY ((workspace, catalog_uid), document_uid)
);

-- vector_stores (collections)
CREATE TABLE wb_vector_store_by_workspace (
    workspace             UUID,
    uid                   UUID,
    name                  text,

    -- vector
    vector_dimension      int,
    vector_similarity     text,

    -- embedding
    embedding_provider    text,
    embedding_model       text,
    embedding_endpoint    text,
    embedding_dimension   int,
    embedding_secret_ref  text,

    -- lexical
    lexical_enabled       boolean,
    lexical_analyzer      text,
    lexical_options       frozen<map<text, text>>,

    -- reranking
    reranking_enabled     boolean,
    reranking_provider    text,
    reranking_model       text,
    reranking_endpoint    text,
    reranking_secret_ref  text,

    created_at            timestamp,
    updated_at            timestamp,

    PRIMARY KEY ((workspace), uid)
);








