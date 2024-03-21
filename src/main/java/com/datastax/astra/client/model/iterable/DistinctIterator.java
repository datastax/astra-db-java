package com.datastax.astra.client.model.iterable;

import io.stargate.sdk.data.client.model.Document;
import io.stargate.sdk.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Iterator to retrieve distincts values of a field in a document.
 * @param <DOC>
 *      working class representing the document
 * @param <FIELD>
 *     working class representing the field to extract from the document.
 */
@Slf4j
public class DistinctIterator<DOC, FIELD> implements Iterator<FIELD> {

    /** Iterable for both find and distinct. */
    private final PageableIterable<DOC> parentIterable;

    /** Iterator on current document page. */
    private Iterator<FIELD> resultsIterator;

    /** The name of the field. */
    private final String fieldName;

    /** The class in use. */
    private final Class<FIELD> fieldClass;

    private Set<FIELD> currentPageRecords;

    /** Existing values. */
    private final Set<FIELD> existingValues = new HashSet<>();

    /**
     * Starting the cursor on an iterable to fetch more pages.
     *
     * @param findIterable
     *      iterable
     */
    public DistinctIterator(PageableIterable<DOC> findIterable, String fieldName, Class<FIELD> fieldClass) {
        this.parentIterable        = findIterable;
        this.fieldName             = fieldName;
        this.fieldClass            = fieldClass;
        initResultIterator();

    }

    /**
     * Mapping of the document to expected field.
     *
     * @param doc
     *      current document
     * @return
     *      extraction of field from document
     */
    private FIELD extractField(DOC doc) {
        return JsonUtils.convertValueForDataApi(doc, Document.class).get(fieldName, fieldClass);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        Set<FIELD> remaining = new HashSet<>();
        boolean hasNext = resultsIterator.hasNext()|| parentIterable.getCurrentPage().getPageState().isPresent();
        if (!hasNext) {
            parentIterable.close();
        }
        return hasNext;
    }

    /**
     * Implementing a logic of iterator combining current page and paging. An local iterator is started on elements
     * of the processing page. If the local iterator is exhausted, the flag 'nextPageState' can tell us is there are
     * more elements to retrieve. if 'nextPageState' is not null the next page is fetch at Iterable level and the
     * local iterator is reinitialized on the new page.
     *
     * @return
     *      next document in the iterator
     */
    @Override
    public FIELD next() {
        if (resultsIterator.hasNext()) {
            parentIterable.getTotalItemProcessed().incrementAndGet();
            FIELD nextValue = resultsIterator.next();
            existingValues.add(nextValue);
            return nextValue;
        } else if (parentIterable.getCurrentPage().getPageState().isPresent()) {
            parentIterable.fetchNextPage();
            initResultIterator();
            // last page is empty after deduplication
            // NoSuchElementException()
            if (currentPageRecords.isEmpty()) {
                log.warn("Last page is empty after deduplication => NoSuchElementException");
            }
            return next();
        }
        parentIterable.close();
        throw new NoSuchElementException("End of the collection");
    }

    /**
     * Items of a page are extracted, deduplicated, then the local resultsIterator is initialize
     * with the items not already processed.
     */
    private void initResultIterator() {
        currentPageRecords = new LinkedHashSet<>();
        currentPageRecords.addAll(parentIterable.getCurrentPage().getResults()
                .stream().map(this::extractField)
                .collect(Collectors.toList()));
        currentPageRecords.removeAll(existingValues);

        this.resultsIterator = currentPageRecords.iterator();
    }
}
