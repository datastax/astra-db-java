package com.datastax.astra.tool;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.SimilarityMetric;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvLoaderCordell {

    // CHANGE ME
    static String csvFilename   = "/Users/cedricklunven/Downloads/knowledge-base.csv";

    // CHANGE ME
    static OpenAiService service = new OpenAiService( "a valid key");

    public static void main(String[] args) {
        DataAPIClient client = new DataAPIClient("a valid token");
        Database db  = client.getDatabase("a valid url");

        List<FileContent> ok = readFileContent(csvFilename);
        for (FileContent fileContent : ok) {
            System.out.println(fileContent.getDocumentId() + " " + fileContent.getTextContent() + " " + fileContent.getVector());
        }

        Collection<FileContent> collection = db
                .createCollection("knowledge_base", 1536, SimilarityMetric.COSINE, FileContent.class);
        collection.insertMany(ok);
    }

    private static float[] embedded(String textContent) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model("text-embedding-ada-002") // Specify the model
                .input(List.of("HELLO")) //FIXME
                .build();
        EmbeddingResult result = service.createEmbeddings(request);
        List<Double> doubles = result.getData().get(0).getEmbedding();
        float[] floats = new float[doubles.size()];
        for (int i = 0; i < doubles.size(); i++) {
            floats[i] = doubles.get(i).floatValue();
        }
        return floats;
    }

    public static List<FileContent> readFileContent(String filePath) {
        List<FileContent> contents = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .withIgnoreEmptyLines()
                     .withIgnoreSurroundingSpaces())) {

            for (CSVRecord record : csvParser) {
                if (record.isConsistent()) {
                    String htmlContent = record.get("OVERVIEW__C");
                    String textContent = Jsoup.parse(htmlContent).text();
                    float[] vector = embedded(textContent);
                    String documentId = record.get("ARTICLECREATEDBYID");
                    contents.add(new FileContent(documentId, textContent, vector));
                } else {
                    System.err.println("Skipping inconsistent record: " + record);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing CSV file: " + e.getMessage());
            e.printStackTrace();
        }
        return contents;
    }

        public static class FileContent {

            @JsonProperty("_id")
            private String documentId;

            @JsonProperty("content")
            private String textContent;

            @JsonProperty("$vector")
            private float[] vector;

            public FileContent(String documentId, String textContent, float[] vector) {
                this.documentId = documentId;
                this.textContent = textContent;
                this.vector = vector;
            }

            public String getDocumentId() {
                return documentId;
            }

            public String getTextContent() {
                return textContent;
            }

            public float[] getVector() {
                return vector;
            }
        }
}

