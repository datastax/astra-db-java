package com.ibm.astra.demo.books;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.spring.DataApiCollectionCrudRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository extends DataApiCollectionCrudRepository<Book, String> {

    @Autowired
    DataAPIClient dataAPIClient;

    Collection<Book> books;

    @PostConstruct
    public void init() {
    }
}
