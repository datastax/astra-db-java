package com.ibm.astra.demo.books;

import com.datastax.astra.client.collections.mapping.DataApiCollection;
import com.datastax.astra.client.collections.mapping.DocumentId;
import com.datastax.astra.client.collections.mapping.Lexical;
import com.datastax.astra.client.collections.mapping.Vectorize;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DataApiCollection("c_book")
public class Book {

    @DocumentId
    String id;

    String title;

    String author;

    boolean is_checked_out;

    @Vectorize
    String vectorize;

    @Lexical
    String lexical;

    @JsonProperty("number_of_pages")
    Integer numberOfPages;

    String genre;

    String description;

    Set<String> genres;

    Map<String, String> metadata;

    // Fluent interface methods
    public Book id(String id) {
        this.id = id;
        return this;
    }

    public Book title(String title) {
        this.title = title;
        return this;
    }

    public Book author(String author) {
        this.author = author;
        return this;
    }

    public Book isCheckedOut(boolean isCheckedOut) {
        this.is_checked_out = isCheckedOut;
        return this;
    }

    public Book vectorize(String vectorize) {
        this.vectorize = vectorize;
        return this;
    }

    public Book lexical(String lexical) {
        this.lexical = lexical;
        return this;
    }

    public Book numberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
        return this;
    }

    public Book genre(String genre) {
        this.genre = genre;
        return this;
    }

    public Book description(String description) {
        this.description = description;
        return this;
    }

    public Book genres(Set<String> genres) {
        this.genres = genres;
        return this;
    }

    public Book metadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }
}
