-------------------------------------------------------------
------------------------ CONFIGURATION ----------------------
-------------------------------------------------------------

CREATE TABLE wb_config_workspaces (
    uid           UUID,
    name          text,
    url           text,
    kind          text, -- astra / hcd / open-rag
    namespace     text,
    credentials   map<text, text>, -- refs only, no secrets
    created_at    timestamp,
    updated_at    timestamp,
    PRIMARY KEY (uid)
);

CREATE TABLE wb_config_knowledge_bases_by_workspace (
    workspace_id            UUID,
    knowledge_base_id       UUID,
    name                    TEXT,
    description             TEXT,
    status                  TEXT,      -- active | draft | deprecated
    embedding_service_id    UUID,
    chunking_service_id     UUID,
    reranking_service_id    UUID,
    language                TEXT,      -- en | fr | multi
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    PRIMARY KEY ((workspace_id), knowledge_base_id)
);


-- List of available chunking service for a particular workspace
CREATE TABLE wb_config_chunking_service_by_workspace (
    workspace_id            UUID,
    chunking_service_id     UUID,
    name                    TEXT,
    description             TEXT,
    status                  TEXT,      -- active | deprecated | experimental
    engine                  TEXT,      -- docling | langchain_ts
    engine_version          TEXT,
    strategy                TEXT,      -- layout | recursive | semantic | hybrid
    max_chunk_size          INT,
    min_chunk_size          INT,
    chunk_unit              TEXT,      -- tokens | characters
    overlap_size            INT,
    overlap_unit            TEXT,
    preserve_structure      BOOLEAN,
    language                TEXT,
    endpoint_base_url       TEXT,
    endpoint_path           TEXT,
    request_timeout_ms      INT,
    max_payload_size_kb     INT,
    auth_type               TEXT,      -- none | api_key | oauth2 | mTLS
    credential_ref          TEXT,
    enable_ocr              BOOLEAN,
    extract_tables          BOOLEAN,
    extract_figures         BOOLEAN,
    reading_order           TEXT,
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    PRIMARY KEY ((workspace_id), chunking_service_id)
);

CREATE TABLE wb_config_embedding_service_by_workspace (
    workspace_id            UUID,
    embedding_service_id    UUID,
    name                    TEXT,
    description             TEXT,
    status                  TEXT,
    provider                TEXT,      -- openai | azure_openai | huggingface | custom
    model_name              TEXT,
    embedding_dimension     INT,
    distance_metric         TEXT,      -- cosine | dot | euclidean
    endpoint_base_url       TEXT,
    endpoint_path           TEXT,
    request_timeout_ms      INT,
    max_batch_size          INT,
    max_input_tokens        INT,
    auth_type               TEXT,
    credential_ref          TEXT,
    supported_languages     SET<TEXT>,
    supported_content       SET<TEXT>,
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    PRIMARY KEY ((workspace_id), embedding_service_id)
);

CREATE TABLE wb_config_reranking_service_by_workspace (
    workspace_id            UUID,
    reranking_service_id    UUID,
    name                    TEXT,
    description             TEXT,
    status                  TEXT,
    provider                TEXT,      -- cohere | openai | huggingface | custom
    engine                  TEXT,      -- cross_encoder | llm | api
    model_name              TEXT,
    model_version           TEXT,
    max_candidates          INT,
    scoring_strategy        TEXT,
    score_normalized        BOOLEAN,
    return_scores           BOOLEAN,
    endpoint_base_url       TEXT,
    endpoint_path           TEXT,
    request_timeout_ms      INT,
    max_batch_size          INT,
    auth_type               TEXT,
    credential_ref          TEXT,
    supported_languages     SET<TEXT>,
    supported_content       SET<TEXT>,
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    PRIMARY KEY ((workspace_id), reranking_service_id)
);

-------------------------------------------------------------
------------------------- RAG -------------------------------
-------------------------------------------------------------

CREATE TABLE wb_rag_documents_by_knowledge_base (
    workspace_id            UUID,
    knowledge_base_id       UUID,
    document_id             UUID,
    source_doc_id           TEXT,
    source_filename         TEXT,
    file_type               TEXT,
    file_size               BIGINT,
    content_hash            TEXT,
    chunk_total             INT,
    status                  TEXT,
    error_message           TEXT,
    ingested_at             TIMESTAMP,
    updated_at              TIMESTAMP,
    metadata                MAP<TEXT, TEXT>,
    PRIMARY KEY ((workspace_id, knowledge_base_id), document_id)
);

CREATE TABLE wb_rag_documents_by_knowledge_base_and_status (
    workspace_id            UUID,
    knowledge_base_id       UUID,
    status                  TEXT,
    document_id             UUID,
    source_filename         TEXT,
    ingested_at             TIMESTAMP,
    PRIMARY KEY ((workspace_id, knowledge_base_id, status), document_id)
);

CREATE TABLE wb_rag_documents_by_content_hash (
    content_hash            TEXT,
    workspace_id            UUID,
    knowledge_base_id       UUID,
    document_id             UUID,
    PRIMARY KEY ((content_hash), workspace_id, knowledge_base_id, document_id)
);

-------------------------------------------------------------
------------------------ AGENTS  ----------------------------
-------------------------------------------------------------

CREATE TABLE wb_config_llm_service_by_workspace (
    workspace_id            UUID,
    llm_service_id          UUID,
    name                    TEXT,
    description             TEXT,
    status                  TEXT,      -- active | deprecated | experimental
    provider                TEXT,      -- openai | azure_openai | anthropic | huggingface | custom
    engine                  TEXT,      -- langchain_ts | direct_rest | sdk
    model_name              TEXT,
    model_version           TEXT,
    context_window_tokens   INT,
    max_output_tokens       INT,
    temperature_min         DOUBLE,
    temperature_max         DOUBLE,
    supports_streaming      BOOLEAN,
    supports_tools          BOOLEAN,   -- function / tool calling
    endpoint_base_url       TEXT,
    endpoint_path           TEXT,
    request_timeout_ms      INT,
    max_batch_size          INT,
    auth_type               TEXT,      -- none | api_key | oauth2 | mTLS
    credential_ref          TEXT,
    supported_languages     SET<TEXT>,
    supported_content       SET<TEXT>, -- text | chat | vision | json
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    PRIMARY KEY ((workspace_id), llm_service_id)
);

CREATE TABLE wb_config_mcp_tools_by_workspace (
    workspace_id            UUID,
    tool_id                 UUID,
    -- identification
    name                    TEXT,
    description             TEXT,
    tool_type               TEXT,      -- mcp | http | function | builtin
    -- invocation contract
    endpoint_base_url       TEXT,
    endpoint_path           TEXT,
    http_method             TEXT,      -- GET | POST
    input_schema            TEXT,      -- JSON schema
    output_schema           TEXT,      -- JSON schema
    -- authentication
    auth_type               TEXT,      -- none | api_key | oauth2 | mTLS
    credential_ref          TEXT,      -- vault reference
    -- metadata
    tags                    SET<TEXT>,
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    PRIMARY KEY ((workspace_id), tool_id)
);


CREATE TABLE wb_agentic_agents_by_workspace (
    workspace_id                UUID,
    agent_id                    UUID,
    name                        TEXT,
    description                 TEXT,
    system_prompt               TEXT,
    user_prompt                 TEXT,
    -- tool usage
    tool_ids                    SET<UUID>, -- references mcp_tools_by_workspace
    -- RAG configuration
    rag_enabled                 BOOLEAN,
    knowledge_base_ids          SET<UUID>, -- agents may use multiple KBs
    rag_max_results             INT,
    rag_min_score               DOUBLE,
    -- Reranking
    rerank_enabled              BOOLEAN,
    reranking_service_id        UUID,
    rerank_max_results          INT,
    -- audit
    created_at                  TIMESTAMP,
    updated_at                  TIMESTAMP,
    PRIMARY KEY ((workspace_id), agent_id)
);

CREATE TABLE wb_agentic_conversations_by_agent (
    workspace_id            UUID,
    agent_id                UUID,
    conversation_id         UUID,
    created_at              TIMESTAMP,
    title                   TEXT,
    PRIMARY KEY ((workspace_id, agent_id), created_at, conversation_id)
);

CREATE TABLE wb_agentic_messages_by_conversation (
    workspace_id            UUID,
    conversation_id         UUID,
    message_ts              TIMESTAMP,
    message_id              UUID,
    role                    TEXT,      -- user | agent | tool | system
    author_id               UUID,      -- agent_id or user_id
    content                 TEXT,
    -- tool invocation tracking
    tool_id                 UUID,
    tool_call_payload       TEXT,
    tool_response           TEXT,
    -- metadata
    token_count             INT,
    metadata                MAP<TEXT, TEXT>,
    PRIMARY KEY ((workspace_id, conversation_id), message_ts)
) WITH CLUSTERING ORDER BY (message_ts ASC);


