
-- 1 backend, multiple stores
CREATE TABLE vector_stores_by_backend (
    backend             text,
    vector_store        text,
    embedding_provider  text,
    embedding_model     text,
    embedding_endpoint  text,
    embedding_dimension int,
    embedding_api_key   text,
    lexical_enabled     text,
    lexical_analyzer    text,
    lexical_options     map<text, text>,
    reranking_provider  text,
    reranking_model     text,
    reranking_endpoint  text,
    reranking_api_key   text,
    PRIMARY KEY ((backend), vector_store)
);

-- list of document ingested
CREATE TABLE documents_by_vector_store (
    backend            text,
    vector_store       text,
    source_doc_id      text,
    source_filename    text,
    file_type          text,
    file_size          bigint,
    -- ingestion
    md5_hash           text,
    chunk_total        int,
    ingested_at        timestamp,
    status             text,
    metadata           map<text, text>,
    PRIMARY KEY ((backend, vector_store), source_doc_id)
);


