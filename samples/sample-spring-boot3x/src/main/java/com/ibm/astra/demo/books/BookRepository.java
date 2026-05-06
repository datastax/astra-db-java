package com.ibm.astra.demo.books;

import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.spring.DataApiCollectionCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookRepository extends DataApiCollectionCrudRepository<Book, String> {

    public List<Book> search(String query, Integer limit) {
        return getCollection()
                .find(new CollectionFindOptions()
                         .sort(Sort.vectorize(query))
                         .projection(Projection.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                         .limit(limit))
                .toList();
    }

}
