package com.datastax.astra.test.unit.collections;

import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.collections.commands.options.CollectionUpdateManyOptions;
import com.datastax.astra.client.collections.commands.results.CollectionUpdateResult;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.internal.api.DataAPIResponse;
import com.datastax.astra.internal.api.DataAPIStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for issue #106 — updateMany does not proceed past first page.
 *
 * <p>The root cause was that {@code nextPageState} was read from {@code data} (always null
 * for updateMany) instead of from {@code status} (where the API actually puts it).
 * This test drives the fix without any real HTTP connection by subclassing {@link Collection}
 * and overriding {@link Collection#runCommand} to return pre-built responses.</p>
 */
class CollectionUpdateManyPaginationTest {

    // --------------------------------------------------
    // Helpers to build fake DataAPIResponse objects
    // --------------------------------------------------

    private static DataAPIResponse pageResponse(int matched, int modified, String nextPageState) {
        DataAPIStatus status = new DataAPIStatus();
        status.setProperty("matchedCount", matched);
        status.setProperty("modifiedCount", modified);
        if (nextPageState != null) {
            status.setProperty("nextPageState", nextPageState);
        }
        DataAPIResponse response = new DataAPIResponse();
        response.setStatus(status);
        return response;
    }

    // --------------------------------------------------
    // Controllable Collection subclass
    // --------------------------------------------------

    private static final DataAPIClientOptions CLIENT_OPTIONS =
            new DataAPIClientOptions().destination(DataAPIDestination.HCD);
    private static final Database STUB_DB =
            new Database("http://localhost:8181/v1/default_keyspace",
                    new DatabaseOptions("test-token", CLIENT_OPTIONS));
    private static final CollectionOptions COL_OPTIONS =
            new CollectionOptions("test-token", CLIENT_OPTIONS);

    /**
     * Collection subclass that feeds responses from a queue instead of making HTTP calls.
     * The {@code super()} call must be first, so we use static fields for the constructor args.
     */
    static class StubCollection extends Collection<Document> {

        private final Queue<DataAPIResponse> responses;

        StubCollection(Queue<DataAPIResponse> responses) {
            super(STUB_DB, "test_col", COL_OPTIONS, Document.class);
            this.responses = responses;
        }

        @Override
        public DataAPIResponse runCommand(Command command, BaseOptions<?> overridingOptions) {
            DataAPIResponse next = responses.poll();
            if (next == null) {
                throw new IllegalStateException("No more stubbed responses — unexpected runCommand call");
            }
            return next;
        }
    }

    // --------------------------------------------------
    // Tests
    // --------------------------------------------------

    @Test
    void updateMany_shouldProcessAllPagesWhenNextPageStateIsInStatus() {
        // Two pages: first page signals more data via nextPageState in status,
        // second page has no nextPageState (pagination ends).
        Queue<DataAPIResponse> responses = new ArrayDeque<>();
        responses.add(pageResponse(50, 50, "PAGE_TOKEN_2"));
        responses.add(pageResponse(30, 30, null));

        StubCollection collection = new StubCollection(responses);

        CollectionUpdateResult result = collection.updateMany(
                new Filter(),
                new Update().set(new Document().append("status", "done")),
                new CollectionUpdateManyOptions());

        assertThat(result.getMatchedCount()).isEqualTo(80);
        assertThat(result.getModifiedCount()).isEqualTo(80);
        // All responses consumed — both pages were requested
        assertThat(responses).isEmpty();
    }

    @Test
    void updateMany_shouldStopAfterSinglePageWhenNoNextPageState() {
        // Single page: no nextPageState in status → loop must terminate after one call.
        Queue<DataAPIResponse> responses = new ArrayDeque<>();
        responses.add(pageResponse(20, 18, null));

        StubCollection collection = new StubCollection(responses);

        CollectionUpdateResult result = collection.updateMany(
                new Filter(),
                new Update().set(new Document().append("active", true)),
                new CollectionUpdateManyOptions());

        assertThat(result.getMatchedCount()).isEqualTo(20);
        assertThat(result.getModifiedCount()).isEqualTo(18);
        assertThat(responses).isEmpty();
    }

    @Test
    void updateMany_shouldHandleThreePagesCorrectly() {
        // Three pages chained together.
        Queue<DataAPIResponse> responses = new ArrayDeque<>();
        responses.add(pageResponse(50, 50, "TOKEN_2"));
        responses.add(pageResponse(50, 50, "TOKEN_3"));
        responses.add(pageResponse(10, 10, null));

        StubCollection collection = new StubCollection(responses);

        CollectionUpdateResult result = collection.updateMany(
                new Filter(),
                new Update().set(new Document().append("processed", true)),
                new CollectionUpdateManyOptions());

        assertThat(result.getMatchedCount()).isEqualTo(110);
        assertThat(result.getModifiedCount()).isEqualTo(110);
        assertThat(responses).isEmpty();
    }
}
