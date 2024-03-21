package com.datastax.astra.client.model.iterable;

import io.stargate.sdk.core.domain.Page;
import io.stargate.sdk.data.client.Collection;
import io.stargate.sdk.data.client.model.Filter;
import io.stargate.sdk.data.client.model.find.FindOptions;
import lombok.Getter;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helping Iteration on Pages and Documents for DataApi.
 */
@Getter
public abstract class PageableIterable<DOC> implements Closeable {

    // -------- Inputs ---------

    /** Reference to the collection in use. */
    protected Collection<DOC> collection;

    /** Check host many has been processed (skip & limit support) */
    protected final AtomicInteger totalItemProcessed = new AtomicInteger(0);

    /** The iterable is active and progressing on the results. */
    protected boolean active = false;

    /** the Iterator is exhausted */
    protected boolean exhausted = false;

    // ----- Page Informations ----

    protected Page<DOC> currentPage;

    protected int currentPageAvailable;

    // ----- Find options ---

    /** Original command, we will edit it to iterate on pages. */
    protected Filter filter;

    /** Original command, we will edit it to iterate on pages. */
    protected FindOptions options;

    /**
     * Fetch the next page if the result.
     *
     * @return
     *      if a new page has been found.
     */
    public boolean fetchNextPage() {
        if (currentPage == null || currentPage.getPageState().isPresent()) {
            if (currentPage != null) {
                options.withPageState(currentPage.getPageState().get());
            }
            this.currentPage  = collection.findPage(filter, options);
        }
        return false;
    }

    /**
     * When no more items available.
     */
    @Override
    public void close() {
        active    = false;
        exhausted = true;
    }

}
