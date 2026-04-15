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
import dev.langchain4j.data.segment.TextSegmentTransformer;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.spi.ServiceHelper;
import dev.langchain4j.spi.data.document.splitter.DocumentSplitterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptation of the EmbeddingStoreIngestor to work with AstraDB and Vectorize.
 */
public class AstraVectorizeIngestor {

    /** Logger fot the class. */
    private static final Logger log = LoggerFactory.getLogger(AstraVectorizeIngestor.class);

    /** Document transformer. */
    private final DocumentTransformer    documentTransformer;

    /** Document Splitter. */
    private final DocumentSplitter       documentSplitter;

    /** Transformer. */
    private final TextSegmentTransformer textSegmentTransformer;

    /** Embedding Store. */
    private final AstraDbEmbeddingStore  embeddingStore;

    /**
     * Ingestor.
     *
     * @param documentTransformer
     *      document transformer
     * @param documentSplitter
     *      document splitter
     * @param textSegmentTransformer
     *      text transformer
     * @param embeddingStore
     *      embedding Store
     */
    public AstraVectorizeIngestor(DocumentTransformer documentTransformer, DocumentSplitter documentSplitter, TextSegmentTransformer textSegmentTransformer, AstraDbEmbeddingStore embeddingStore) {
        this.documentTransformer = documentTransformer;
        this.documentSplitter = (DocumentSplitter) Utils.getOrDefault(documentSplitter, AstraVectorizeIngestor::loadDocumentSplitter);
        this.textSegmentTransformer = textSegmentTransformer;
        this.embeddingStore = ValidationUtils.ensureNotNull(embeddingStore, "embeddingStore");
    }

    /**
     * Split document.
     *
     * @return
     *      document splitter
     */
    private static DocumentSplitter loadDocumentSplitter() {
        Collection<DocumentSplitterFactory> factories = ServiceHelper.loadFactories(DocumentSplitterFactory.class);
        if (factories.size() > 1) {
            throw new RuntimeException("Conflict: multiple document splitters have been found in the classpath. " +
                    "Please explicitly specify the one you wish to use.");
        } else {
            Iterator<DocumentSplitterFactory> var1 = factories.iterator();
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

    /**
     * Ingest the document.
     * @param document
     *      document
     * @param embeddingStore
     *      embedding store
     */
    public static void ingest(Document document, AstraDbEmbeddingStore embeddingStore) {
        builder().embeddingStore(embeddingStore).build().ingest(document);
    }

    /**
     * Ingest a list of documents.
     *
     * @param documents
     *      list of documents
     * @param embeddingStore
     *      embedding store
     */
    public static void ingest(List<Document> documents, AstraDbEmbeddingStore embeddingStore) {
        builder().embeddingStore(embeddingStore).build().ingest(documents);
    }

    /**
     * Ingest one document
     *
     * @param document
     *      a single document
     */
    public void ingest(Document document) {
        this.ingest(Collections.singletonList(document));
    }

    /**
     * Ingest a list of documents.
     *
     * @param documents
     *      list of documents
     */
    public void ingest(Document... documents) {
        this.ingest(Arrays.asList(documents));
    }


    /**
     * Ingest a list of documents.
     *
     * @param documents
     *      list of documents
     */
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

    /**
     * Create the Builder.
     *
     * @return
     *      builder for the ingestor
     */
    public static AstraVectorizeIngestor.Builder builder() {
        return new AstraVectorizeIngestor.Builder();
    }

    /**
     * Internal Builder.
     */
    public static class Builder {

        /* transformer. */
        private DocumentTransformer documentTransformer;

        /* splitter. */
        private DocumentSplitter documentSplitter;

        /* text transformer. */
        private TextSegmentTransformer textSegmentTransformer;

        /* store. */
        private AstraDbEmbeddingStore embeddingStore;

        /**
         * Internal Constructor
         */
        public Builder() {
        }

        /**
         * Accessor for documentTransformer.
         * @param documentTransformer
         *      documentTransformer
         * @return
         *      current ref
         */
        public AstraVectorizeIngestor.Builder documentTransformer(DocumentTransformer documentTransformer) {
            this.documentTransformer = documentTransformer;
            return this;
        }

        /**
         * Accessor for documentSplitter.
         * @param documentSplitter
         *      documentSplitter
         * @return
         *      current ref
         */
        public AstraVectorizeIngestor.Builder documentSplitter(DocumentSplitter documentSplitter) {
            this.documentSplitter = documentSplitter;
            return this;
        }

        /**
         * Accessor for textSegmentTransformer.
         * @param textSegmentTransformer
         *      textSegmentTransformer
         * @return
         *      current ref
         */
        public AstraVectorizeIngestor.Builder textSegmentTransformer(TextSegmentTransformer textSegmentTransformer) {
            this.textSegmentTransformer = textSegmentTransformer;
            return this;
        }

        /**
         * Accessor for embeddingStore.
         * @param embeddingStore
         *      embeddingStore
         * @return
         *      current ref
         */
        public AstraVectorizeIngestor.Builder embeddingStore(AstraDbEmbeddingStore embeddingStore) {
            this.embeddingStore = embeddingStore;
            return this;
        }

        /**
         * Operation to build.
         *
         * @return
         *      instance of the request.
         */
        public AstraVectorizeIngestor build() {
            return new AstraVectorizeIngestor(
                    this.documentTransformer,
                    this.documentSplitter,
                    this.textSegmentTransformer,
                    this.embeddingStore);
        }
    }
}
