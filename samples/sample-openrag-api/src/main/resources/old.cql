CREATE TABLE conversations (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL DEFAULT 'New Conversation',
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  message_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE messages (
  id TEXT PRIMARY KEY,
  conversation_id TEXT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  role TEXT NOT NULL,
  content TEXT NOT NULL,
  timestamp TEXT NOT NULL,
  search_mode TEXT,
  latency_ms INTEGER,
  pipeline_steps TEXT,
  sources TEXT
);



CREATE TABLE ingested_documents (
  source_doc_id TEXT PRIMARY KEY,
  source_filename TEXT NOT NULL,
  file_type TEXT NOT NULL,
  file_size INTEGER NOT NULL,
  md5_hash TEXT NOT NULL,
  chunk_total INTEGER NOT NULL,
  collection TEXT NOT NULL,
  ingested_at TEXT NOT NULL,
  metadata TEXT NOT NULL DEFAULT '{}'
);
