package com.ibm.openrag;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

/**
 * Java SDK Client for OpenRAG API
 * 
 * This client provides access to all OpenRAG API endpoints:
 * 
 * <h2>Available Endpoints:</h2>
 * 
 * <h3>1. Chat Operations (/api/v1/chat)</h3>
 * <ul>
 *   <li><b>POST /api/v1/chat</b> - Send a chat message (streaming or non-streaming)
 *       <ul>
 *         <li>Parameters: message, chat_id (optional), filters (optional), limit, score_threshold, filter_id (optional), stream</li>
 *         <li>Returns: ChatResponse with response text, chat_id, and sources</li>
 *       </ul>
 *   </li>
 *   <li><b>GET /api/v1/chat</b> - List all conversations
 *       <ul>
 *         <li>Returns: List of conversations with metadata (id, title, created_at, last_activity, message_count)</li>
 *       </ul>
 *   </li>
 *   <li><b>GET /api/v1/chat/{chat_id}</b> - Get conversation details with full message history
 *       <ul>
 *         <li>Returns: ConversationDetail with all messages</li>
 *       </ul>
 *   </li>
 *   <li><b>DELETE /api/v1/chat/{chat_id}</b> - Delete a conversation
 *       <ul>
 *         <li>Returns: Success status</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h3>2. Search Operations (/api/v1/search)</h3>
 * <ul>
 *   <li><b>POST /api/v1/search</b> - Perform semantic search on documents
 *       <ul>
 *         <li>Parameters: query, filters (optional), limit, score_threshold, filter_id (optional)</li>
 *         <li>Returns: SearchResponse with list of SearchResult (content, score, metadata)</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h3>3. Document Operations (/api/v1/documents)</h3>
 * <ul>
 *   <li><b>POST /api/v1/documents/ingest</b> - Ingest a document into knowledge base
 *       <ul>
 *         <li>Parameters: file (multipart upload)</li>
 *         <li>Returns: IngestResponse with task_id for tracking</li>
 *       </ul>
 *   </li>
 *   <li><b>GET /api/v1/tasks/{task_id}</b> - Get ingestion task status
 *       <ul>
 *         <li>Returns: IngestTaskStatus (status: pending/processing/completed/failed)</li>
 *       </ul>
 *   </li>
 *   <li><b>DELETE /api/v1/documents</b> - Delete a document by filename
 *       <ul>
 *         <li>Parameters: filename</li>
 *         <li>Returns: DeleteDocumentResponse with deleted chunk count</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h3>4. Settings Operations (/api/v1/settings)</h3>
 * <ul>
 *   <li><b>GET /api/v1/settings</b> - Get current OpenRAG configuration
 *       <ul>
 *         <li>Returns: SettingsResponse with agent and knowledge settings</li>
 *       </ul>
 *   </li>
 *   <li><b>POST /api/v1/settings</b> - Update OpenRAG configuration
 *       <ul>
 *         <li>Parameters: Settings object with fields to update</li>
 *         <li>Returns: SettingsUpdateResponse with success message</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h3>5. Models Operations (/api/v1/models)</h3>
 * <ul>
 *   <li><b>GET /api/v1/models/{provider}</b> - List available models for a provider
 *       <ul>
 *         <li>Parameters: provider (openai, anthropic, ollama, watsonx)</li>
 *         <li>Returns: ModelsResponse with language_models and embedding_models lists</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h3>6. Knowledge Filter Operations (/api/v1/knowledge-filters)</h3>
 * <ul>
 *   <li><b>POST /api/v1/knowledge-filters</b> - Create a new knowledge filter
 *       <ul>
 *         <li>Parameters: name, description, queryData (JSON string with query, filters, limit, score_threshold)</li>
 *         <li>Returns: CreateKnowledgeFilterResponse with filter ID</li>
 *       </ul>
 *   </li>
 *   <li><b>POST /api/v1/knowledge-filters/search</b> - Search for knowledge filters
 *       <ul>
 *         <li>Parameters: query (optional), limit</li>
 *         <li>Returns: List of matching KnowledgeFilter objects</li>
 *       </ul>
 *   </li>
 *   <li><b>GET /api/v1/knowledge-filters/{filter_id}</b> - Get a specific knowledge filter
 *       <ul>
 *         <li>Returns: KnowledgeFilter object</li>
 *       </ul>
 *   </li>
 *   <li><b>PUT /api/v1/knowledge-filters/{filter_id}</b> - Update a knowledge filter
 *       <ul>
 *         <li>Parameters: name, description, queryData (fields to update)</li>
 *         <li>Returns: Success status</li>
 *       </ul>
 *   </li>
 *   <li><b>DELETE /api/v1/knowledge-filters/{filter_id}</b> - Delete a knowledge filter
 *       <ul>
 *         <li>Returns: Success status</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h2>Authentication:</h2>
 * <ul>
 *   <li>API Key via X-API-Key header (from OPENRAG_API_KEY environment variable)</li>
 *   <li>Or custom headers (X-Username, X-Api-Key) for IBM auth mode</li>
 * </ul>
 * 
 * <h2>Configuration:</h2>
 * <ul>
 *   <li>Base URL: Default http://localhost:3000 (from OPENRAG_URL environment variable)</li>
 *   <li>Timeout: Configurable request timeout</li>
 * </ul>
 * 
 * @author OpenRAG SDK
 * @version 1.0.0
 */
public class OpenRagClient {

    private final String baseUrl;
    private final String apiKey;
    private final HttpClient httpClient;
    private final Duration timeout;

    /**
     * Creates a new OpenRAG client with default configuration.
     * Uses OPENRAG_URL and OPENRAG_API_KEY environment variables.
     */
    public OpenRagClient() {
        this(
            System.getenv().getOrDefault("OPENRAG_URL", "http://localhost:3000"),
            System.getenv("OPENRAG_API_KEY"),
            Duration.ofSeconds(30)
        );
    }

    /**
     * Creates a new OpenRAG client with custom configuration.
     * 
     * @param baseUrl Base URL for the OpenRAG API
     * @param apiKey API key for authentication
     * @param timeout Request timeout duration
     */
    public OpenRagClient(String baseUrl, String apiKey, Duration timeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(timeout)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    // ==================== CHAT OPERATIONS ====================

    /**
     * Send a chat message (non-streaming).
     * 
     * @param message The message to send
     * @return JSON response as String
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String sendChatMessage(String message) throws IOException, InterruptedException {
        return sendChatMessage(message, null, null, 10, 0.0, null);
    }

    /**
     * Send a chat message with full options (non-streaming).
     * 
     * @param message The message to send
     * @param chatId Optional conversation ID to continue
     * @param filters Optional search filters (JSON string)
     * @param limit Maximum number of search results
     * @param scoreThreshold Minimum search score threshold
     * @param filterId Optional knowledge filter ID
     * @return JSON response as String
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String sendChatMessage(String message, String chatId, String filters, 
                                  int limit, double scoreThreshold, String filterId) 
            throws IOException, InterruptedException {
        
        StringBuilder jsonBody = new StringBuilder("{\"message\":\"" + escapeJson(message) + "\"");
        jsonBody.append(",\"stream\":false");
        jsonBody.append(",\"limit\":").append(limit);
        jsonBody.append(",\"score_threshold\":").append(scoreThreshold);
        
        if (chatId != null) {
            jsonBody.append(",\"chat_id\":\"").append(escapeJson(chatId)).append("\"");
        }
        if (filters != null) {
            jsonBody.append(",\"filters\":").append(filters);
        }
        if (filterId != null) {
            jsonBody.append(",\"filter_id\":\"").append(escapeJson(filterId)).append("\"");
        }
        jsonBody.append("}");

        return executeRequest("POST", "/api/v1/chat", jsonBody.toString());
    }

    /**
     * List all conversations.
     * 
     * @return JSON response with conversation list
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String listConversations() throws IOException, InterruptedException {
        return executeRequest("GET", "/api/v1/chat", null);
    }

    /**
     * Get a specific conversation with full message history.
     * 
     * @param chatId The conversation ID
     * @return JSON response with conversation details
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String getConversation(String chatId) throws IOException, InterruptedException {
        return executeRequest("GET", "/api/v1/chat/" + chatId, null);
    }

    /**
     * Delete a conversation.
     * 
     * @param chatId The conversation ID to delete
     * @return JSON response with success status
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String deleteConversation(String chatId) throws IOException, InterruptedException {
        return executeRequest("DELETE", "/api/v1/chat/" + chatId, null);
    }

    // ==================== SEARCH OPERATIONS ====================

    /**
     * Perform semantic search on documents.
     * 
     * @param query The search query text
     * @return JSON response with search results
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String search(String query) throws IOException, InterruptedException {
        return search(query, null, 10, 0.0, null);
    }

    /**
     * Perform semantic search with full options.
     * 
     * @param query The search query text
     * @param filters Optional search filters (JSON string)
     * @param limit Maximum number of results
     * @param scoreThreshold Minimum score threshold
     * @param filterId Optional knowledge filter ID
     * @return JSON response with search results
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String search(String query, String filters, int limit, 
                        double scoreThreshold, String filterId) 
            throws IOException, InterruptedException {
        
        StringBuilder jsonBody = new StringBuilder("{\"query\":\"" + escapeJson(query) + "\"");
        jsonBody.append(",\"limit\":").append(limit);
        jsonBody.append(",\"score_threshold\":").append(scoreThreshold);
        
        if (filters != null) {
            jsonBody.append(",\"filters\":").append(filters);
        }
        if (filterId != null) {
            jsonBody.append(",\"filter_id\":\"").append(escapeJson(filterId)).append("\"");
        }
        jsonBody.append("}");

        return executeRequest("POST", "/api/v1/search", jsonBody.toString());
    }

    // ==================== DOCUMENT OPERATIONS ====================

    /**
     * Ingest a document into the knowledge base.
     * 
     * @param filePath Path to the file to ingest
     * @return JSON response with task_id
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String ingestDocument(String filePath) throws IOException, InterruptedException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        byte[] fileContent = Files.readAllBytes(file.toPath());
        
        StringBuilder body = new StringBuilder();
        body.append("--").append(boundary).append("\r\n");
        body.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
            .append(file.getName()).append("\"\r\n");
        body.append("Content-Type: application/octet-stream\r\n\r\n");
        
        // Note: This is a simplified version. For production, use a proper multipart library
        String bodyStr = body.toString();
        byte[] bodyBytes = bodyStr.getBytes();
        byte[] endBoundary = ("\r\n--" + boundary + "--\r\n").getBytes();
        
        byte[] fullBody = new byte[bodyBytes.length + fileContent.length + endBoundary.length];
        System.arraycopy(bodyBytes, 0, fullBody, 0, bodyBytes.length);
        System.arraycopy(fileContent, 0, fullBody, bodyBytes.length, fileContent.length);
        System.arraycopy(endBoundary, 0, fullBody, bodyBytes.length + fileContent.length, endBoundary.length);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/v1/documents/ingest"))
            .timeout(timeout)
            .header("X-API-Key", apiKey)
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(fullBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Get the status of an ingestion task.
     * 
     * @param taskId The task ID from ingest response
     * @return JSON response with task status
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String getTaskStatus(String taskId) throws IOException, InterruptedException {
        return executeRequest("GET", "/api/v1/tasks/" + taskId, null);
    }

    /**
     * Delete a document from the knowledge base.
     * 
     * @param filename Name of the file to delete
     * @return JSON response with deleted chunk count
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String deleteDocument(String filename) throws IOException, InterruptedException {
        String jsonBody = "{\"filename\":\"" + escapeJson(filename) + "\"}";
        return executeRequest("DELETE", "/api/v1/documents", jsonBody);
    }

    // ==================== SETTINGS OPERATIONS ====================

    /**
     * Get current OpenRAG configuration.
     * 
     * @return JSON response with settings
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String getSettings() throws IOException, InterruptedException {
        return executeRequest("GET", "/api/v1/settings", null);
    }

    /**
     * Update OpenRAG configuration.
     * 
     * @param settingsJson JSON string with settings to update
     * @return JSON response with success message
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String updateSettings(String settingsJson) throws IOException, InterruptedException {
        return executeRequest("POST", "/api/v1/settings", settingsJson);
    }

    // ==================== MODELS OPERATIONS ====================

    /**
     * List available models for a provider.
     * 
     * @param provider Provider name (openai, anthropic, ollama, watsonx)
     * @return JSON response with language_models and embedding_models
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String listModels(String provider) throws IOException, InterruptedException {
        return executeRequest("GET", "/api/v1/models/" + provider, null);
    }

    // ==================== KNOWLEDGE FILTER OPERATIONS ====================

    /**
     * Create a new knowledge filter.
     * 
     * @param name Filter name
     * @param description Filter description
     * @param queryDataJson JSON string with query data
     * @return JSON response with filter ID
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String createKnowledgeFilter(String name, String description, String queryDataJson) 
            throws IOException, InterruptedException {
        
        String jsonBody = String.format(
            "{\"name\":\"%s\",\"description\":\"%s\",\"queryData\":%s}",
            escapeJson(name), escapeJson(description), queryDataJson
        );
        return executeRequest("POST", "/api/v1/knowledge-filters", jsonBody);
    }

    /**
     * Search for knowledge filters.
     * 
     * @param query Search query (optional, empty string for all)
     * @param limit Maximum number of results
     * @return JSON response with matching filters
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String searchKnowledgeFilters(String query, int limit) 
            throws IOException, InterruptedException {
        
        String jsonBody = String.format("{\"query\":\"%s\",\"limit\":%d}", escapeJson(query), limit);
        return executeRequest("POST", "/api/v1/knowledge-filters/search", jsonBody);
    }

    /**
     * Get a specific knowledge filter.
     * 
     * @param filterId The filter ID
     * @return JSON response with filter details
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String getKnowledgeFilter(String filterId) throws IOException, InterruptedException {
        return executeRequest("GET", "/api/v1/knowledge-filters/" + filterId, null);
    }

    /**
     * Update a knowledge filter.
     * 
     * @param filterId The filter ID
     * @param updateJson JSON string with fields to update
     * @return JSON response with success status
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String updateKnowledgeFilter(String filterId, String updateJson) 
            throws IOException, InterruptedException {
        return executeRequest("PUT", "/api/v1/knowledge-filters/" + filterId, updateJson);
    }

    /**
     * Delete a knowledge filter.
     * 
     * @param filterId The filter ID to delete
     * @return JSON response with success status
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String deleteKnowledgeFilter(String filterId) throws IOException, InterruptedException {
        return executeRequest("DELETE", "/api/v1/knowledge-filters/" + filterId, null);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Execute an HTTP request.
     * 
     * @param method HTTP method (GET, POST, PUT, DELETE)
     * @param path API endpoint path
     * @param body Request body (null for GET/DELETE without body)
     * @return Response body as String
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    private String executeRequest(String method, String path, String body) 
            throws IOException, InterruptedException {
        
        String fullUrl = baseUrl + path;
        System.out.println("DEBUG: Making " + method + " request to: " + fullUrl);
        System.out.println("DEBUG: API Key: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null"));
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(fullUrl))
            .timeout(timeout)
            .header("X-API-Key", apiKey);
        
        // Only add Content-Type for requests with body
        if (body != null && !method.equalsIgnoreCase("GET")) {
            requestBuilder.header("Content-Type", "application/json");
        }

        switch (method.toUpperCase()) {
            case "GET":
                requestBuilder.GET();
                break;
            case "POST":
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}"));
                break;
            case "PUT":
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}"));
                break;
            case "DELETE":
                if (body != null) {
                    requestBuilder.method("DELETE", HttpRequest.BodyPublishers.ofString(body));
                } else {
                    requestBuilder.DELETE();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        HttpRequest request = requestBuilder.build();
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("DEBUG: Response status: " + response.statusCode());
            
            if (response.statusCode() >= 400) {
                throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
            }
            
            return response.body();
        } catch (IOException e) {
            System.err.println("DEBUG: IOException occurred: " + e.getClass().getName() + ": " + e.getMessage());
            System.err.println("DEBUG: Full URL was: " + fullUrl);
            throw e;
        }
    }

    /**
     * Escape JSON string values.
     * 
     * @param value String to escape
     * @return Escaped string
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * Wait for an ingestion task to complete.
     * 
     * @param taskId The task ID to wait for
     * @param maxWaitSeconds Maximum seconds to wait
     * @return Final task status JSON
     * @throws IOException If request fails
     * @throws InterruptedException If request is interrupted
     */
    public String waitForIngestion(String taskId, int maxWaitSeconds) 
            throws IOException, InterruptedException {
        int elapsed = 0;
        int pollInterval = 2; // seconds
        
        while (elapsed < maxWaitSeconds) {
            String status = getTaskStatus(taskId);
            
            // Check if completed or failed
            if (status.contains("\"status\":\"completed\"") || status.contains("\"status\":\"failed\"")) {
                return status;
            }
            
            Thread.sleep(pollInterval * 1000);
            elapsed += pollInterval;
            System.out.println("Waiting for ingestion... (" + elapsed + "s)");
        }
        
        throw new IOException("Ingestion timeout after " + maxWaitSeconds + " seconds");
    }

    /**
     * Extract task_id from ingest response JSON.
     * 
     * @param json JSON response string
     * @return task_id or null if not found
     */
    public String extractTaskId(String json) {
        // Simple JSON parsing for task_id
        int taskIdIndex = json.indexOf("\"task_id\"");
        if (taskIdIndex == -1) return null;
        
        int colonIndex = json.indexOf(":", taskIdIndex);
        int quoteStart = json.indexOf("\"", colonIndex);
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        
        if (quoteStart != -1 && quoteEnd != -1) {
            return json.substring(quoteStart + 1, quoteEnd);
        }
        return null;
    }

    /**
     * Extract response text from chat response JSON.
     * 
     * @param json JSON response string
     * @return response text or the full JSON if parsing fails
     */
    public String extractChatResponse(String json) {
        // Simple JSON parsing for response field
        int responseIndex = json.indexOf("\"response\"");
        if (responseIndex == -1) return json;
        
        int colonIndex = json.indexOf(":", responseIndex);
        int quoteStart = json.indexOf("\"", colonIndex);
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        
        if (quoteStart != -1 && quoteEnd != -1) {
            return json.substring(quoteStart + 1, quoteEnd)
                      .replace("\\n", "\n")
                      .replace("\\\"", "\"");
        }
        return json;
    }
}