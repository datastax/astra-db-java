package com.ibm.openrag;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Properties;

public class OpenRagClientDemo {

    // ==================== DEMO MAIN METHOD ====================

    /**
     * Demo usage of the OpenRAG client with document ingestion and question answering.
     *
     * This example demonstrates:
     * 1. Loading configuration from application.properties
     * 2. Creating a sample document
     * 3. Ingesting it into OpenRAG
     * 4. Waiting for ingestion to complete
     * 5. Asking a question about the document
     * 6. Performing a semantic search
     */
    public static void main(String[] args) {
        try {
            // Load configuration from application.properties
            System.out.println("📋 Loading configuration from application.properties...");
            Properties props = new Properties();
            try (InputStream input = OpenRagClientDemo.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (input == null) {
                    System.err.println("❌ Unable to find application.properties");
                    return;
                }
                props.load(input);
            }
            
            String apiKey = props.getProperty("openrag.apikey");
            String url = props.getProperty("openrag.url", "http://localhost:3000");
            
            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("❌ Error: openrag.apikey not found in application.properties");
                return;
            }
            
            System.out.println("✓ Configuration loaded:");
            System.out.println("  - URL: " + url);
            System.out.println("  - API Key: " + apiKey.substring(0, 10) + "..." + "\n");
            
            // Initialize client with properties
            OpenRagClient client = new OpenRagClient(url, apiKey, Duration.ofSeconds(30));

            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║         OpenRAG Java SDK - Complete Demo                  ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");

            // Step 0: Verify connection by listing conversations
            System.out.println("🔌 Step 0: Verifying connection to OpenRAG...");
            try {
                String conversations = client.listConversations();
                System.out.println("✓ Connection successful!");
                System.out.println("Existing conversations: " + conversations + "\n");
            } catch (Exception e) {
                System.err.println("❌ Connection failed: " + e.getMessage());
                System.err.println("Please verify your openrag.url and openrag.apikey in application.properties\n");
                return;
            }

            // Step 1: Create a sample document
            System.out.println("📄 Step 1: Creating sample document...");
            String sampleContent = """
                # Machine Learning Guide
                
                ## What is Machine Learning?
                Machine Learning (ML) is a subset of artificial intelligence that enables 
                systems to learn and improve from experience without being explicitly programmed.
                
                ## Types of Machine Learning
                1. **Supervised Learning**: Learning from labeled data
                2. **Unsupervised Learning**: Finding patterns in unlabeled data
                3. **Reinforcement Learning**: Learning through trial and error
                
                ## Popular Algorithms
                - Linear Regression
                - Decision Trees
                - Neural Networks
                - Support Vector Machines
                
                ## Applications
                Machine learning is used in:
                - Image recognition
                - Natural language processing
                - Recommendation systems
                - Autonomous vehicles
                """;

            File tempFile = File.createTempFile("ml-guide-", ".md");
            Files.writeString(tempFile.toPath(), sampleContent);
            System.out.println("✓ Created: " + tempFile.getName() + "\n");

            // Step 2: Ingest the document
            System.out.println("📤 Step 2: Ingesting document into OpenRAG...");
            String ingestResponse = client.ingestDocument(tempFile.getAbsolutePath());
            System.out.println("Response: " + ingestResponse);

            String taskId = client.extractTaskId(ingestResponse);
            if (taskId != null) {
                System.out.println("Task ID: " + taskId + "\n");

                // Step 3: Wait for ingestion to complete
                System.out.println("⏳ Step 3: Waiting for ingestion to complete...");
                String finalStatus = client.waitForIngestion(taskId, 60);
                System.out.println("✓ Ingestion completed!");
                System.out.println("Status: " + finalStatus + "\n");
            }

            // Give the system a moment to index
            Thread.sleep(2000);

            // Step 4: Ask a question about the document
            System.out.println("💬 Step 4: Asking question about the document...");
            String question = "What are the three types of machine learning?";
            System.out.println("Question: " + question);

            String chatResponse = client.sendChatMessage(question);
            String answer = client.extractChatResponse(chatResponse);
            System.out.println("\n📝 Answer:");
            System.out.println(answer);
            System.out.println("\nFull Response: " + chatResponse + "\n");

            // Step 5: Perform a semantic search
            System.out.println("🔍 Step 5: Performing semantic search...");
            String searchQuery = "neural networks applications";
            System.out.println("Search Query: " + searchQuery);

            String searchResponse = client.search(searchQuery);
            System.out.println("\n📊 Search Results:");
            System.out.println(searchResponse + "\n");

            // Step 6: List conversations
            System.out.println("📋 Step 6: Listing conversations...");
            String conversations = client.listConversations();
            System.out.println(conversations + "\n");

            // Step 7: Get settings
            System.out.println("⚙️  Step 7: Getting OpenRAG settings...");
            String settings = client.getSettings();
            System.out.println(settings + "\n");

            // Cleanup
            System.out.println("🧹 Cleanup: Deleting sample document...");
            String deleteResponse = client.deleteDocument(tempFile.getName());
            System.out.println(deleteResponse);
            tempFile.delete();

            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║              ✓ Demo completed successfully!               ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println("\n❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}