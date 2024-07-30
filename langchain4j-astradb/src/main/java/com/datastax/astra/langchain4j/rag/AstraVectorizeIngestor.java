package com.datastax.astra.langchain4j.rag;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.langchain4j.store.embedding.AstraDbEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.DocumentTransformer;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.segment.TextSegmentTransformer;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.spi.ServiceHelper;
import dev.langchain4j.spi.data.document.splitter.DocumentSplitterFactory;
import dev.langchain4j.spi.model.embedding.EmbeddingModelFactory;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.DocumentTransformer;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.segment.TextSegmentTransformer;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.spi.ServiceHelper;
import dev.langchain4j.spi.data.document.splitter.DocumentSplitterFactory;
import dev.langchain4j.spi.model.embedding.EmbeddingModelFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptation of the EmbeddingStoreIngestor to work with AstraDB and Vectorize
 */
public class AstraVectorizeIngestor {

    private static final Logger log = LoggerFactory
            .getLogger(AstraVectorizeIngestor.class);

    private final DocumentTransformer    documentTransformer;
    private final DocumentSplitter       documentSplitter;
    private final TextSegmentTransformer textSegmentTransformer;
    private final AstraDbEmbeddingStore  embeddingStore;

    public AstraVectorizeIngestor(DocumentTransformer documentTransformer, DocumentSplitter documentSplitter, TextSegmentTransformer textSegmentTransformer, AstraDbEmbeddingStore embeddingStore) {
        this.documentTransformer = documentTransformer;
        this.documentSplitter = (DocumentSplitter) Utils.getOrDefault(documentSplitter, AstraVectorizeIngestor::loadDocumentSplitter);
        this.textSegmentTransformer = textSegmentTransformer;
        this.embeddingStore = ValidationUtils.ensureNotNull(embeddingStore, "embeddingStore");
    }

    private static DocumentSplitter loadDocumentSplitter() {
        Collection<DocumentSplitterFactory> factories = ServiceHelper.loadFactories(DocumentSplitterFactory.class);
        if (factories.size() > 1) {
            throw new RuntimeException("Conflict: multiple document splitters have been found in the classpath. Please explicitly specify the one you wish to use.");
        } else {
            Iterator var1 = factories.iterator();
            if (var1.hasNext()) {
                DocumentSplitterFactory factory = (DocumentSplitterFactory)var1.next();
                DocumentSplitter documentSplitter = factory.create();
                log.debug("Loaded the following document splitter through SPI: {}", documentSplitter);
                return documentSplitter;
            } else {
                return null;
            }
        }
    }

    public static void ingest(Document document, AstraDbEmbeddingStore embeddingStore) {
        builder().embeddingStore(embeddingStore).build().ingest(document);
    }

    public static void ingest(List<Document> documents, AstraDbEmbeddingStore embeddingStore) {
        builder().embeddingStore(embeddingStore).build().ingest(documents);
    }

    public void ingest(Document document) {
        this.ingest(Collections.singletonList(document));
    }

    public void ingest(Document... documents) {
        this.ingest(Arrays.asList(documents));
    }

    public void ingest(List<Document> documents) {
        log.debug("Starting to ingest {} documents", documents.size());
        if (this.documentTransformer != null) {
            documents = this.documentTransformer.transformAll(documents);
            log.debug("Documents were transformed into {} documents", documents.size());
        }

        List segments;
        if (this.documentSplitter != null) {
            segments = this.documentSplitter.splitAll(documents);
            log.debug("Documents were split into {} text segments", segments.size());
        } else {
            segments = (List)documents.stream().map(Document::toTextSegment).collect(Collectors.toList());
        }

        if (this.textSegmentTransformer != null) {
            segments = this.textSegmentTransformer.transformAll(segments);
            log.debug("Text segments were transformed into {} text segments", documents.size());
        }
        List<Embedding> embeddings = null;
        log.debug("Starting to store {} text segments into the embedding store", segments.size());
        this.embeddingStore.addAllVectorize(segments);
        log.debug("Finished storing {} text segments into the embedding store", segments.size());
    }

    public static AstraVectorizeIngestor.Builder builder() {
        return new AstraVectorizeIngestor.Builder();
    }

    public static class Builder {
        private DocumentTransformer documentTransformer;
        private DocumentSplitter documentSplitter;
        private TextSegmentTransformer textSegmentTransformer;
        private AstraDbEmbeddingStore embeddingStore;

        public Builder() {
        }

        public AstraVectorizeIngestor.Builder documentTransformer(DocumentTransformer documentTransformer) {
            this.documentTransformer = documentTransformer;
            return this;
        }

        public AstraVectorizeIngestor.Builder documentSplitter(DocumentSplitter documentSplitter) {
            this.documentSplitter = documentSplitter;
            return this;
        }

        public AstraVectorizeIngestor.Builder textSegmentTransformer(TextSegmentTransformer textSegmentTransformer) {
            this.textSegmentTransformer = textSegmentTransformer;
            return this;
        }

        public AstraVectorizeIngestor.Builder embeddingStore(AstraDbEmbeddingStore embeddingStore) {
            this.embeddingStore = embeddingStore;
            return this;
        }

        public AstraVectorizeIngestor build() {
            return new AstraVectorizeIngestor(this.documentTransformer, this.documentSplitter, this.textSegmentTransformer, this.embeddingStore);
        }
    }
}
