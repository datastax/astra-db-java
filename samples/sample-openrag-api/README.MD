
### Specification


1. Chat (/api/v1/chat):
    - POST: Create chat (streaming/non-streaming)
    - GET: List conversations
    - GET /{chat_id}: Get conversation details
    - DELETE /{chat_id}: Delete conversation

2. Search (/api/v1/search):
    - POST: Semantic search query

3. Documents (/api/v1/documents):
    - POST /ingest: Ingest document
    - DELETE: Delete document
    - GET /api/v1/tasks/{task_id}: Get ingestion task status

4. Settings (/api/v1/settings):
    - GET: Get settings
    - POST: Update settings

5. Models (/api/v1/models/{provider}):
    - GET: List models for provider

6. Knowledge Filters (/api/v1/knowledge-filters):
    - POST: Create filter
    - POST /search: Search filters
    - GET /{filter_id}: Get filter
    - PUT /{filter_id}: Update filter
    - DELETE /{filter_id}: Delete filter