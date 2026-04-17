package com.ibm.astra.demo.books;

import com.datastax.astra.client.collections.mapping.DataApiCollection;
import com.datastax.astra.client.collections.mapping.DocumentId;
import com.datastax.astra.client.collections.mapping.Vectorize;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DataApiCollection(
   name = "c_books_vectorize_nvidia",
   vectorDimension = 1024,
   vectorizeModel = "NV-Embed-QA",
   vectorizeProvider = "nvidia"
)
@Schema(name = "Book", description = "Book document stored in Astra DB")
public class Book {

    @DocumentId
    @Schema(description = "Unique identifier of the book", example = "book-1984")
    private String id;

    @Schema(description = "Title of the book", example = "1984")
    private String title;

    @Schema(description = "Author name", example = "George Orwell")
    private String author;

    @JsonProperty("checked_out")
    @Schema(description = "Whether the book is currently checked out", example = "false")
    private boolean checkedOut;

    @JsonProperty("number_of_pages")
    @Schema(description = "Number of pages", example = "328")
    private Integer numberOfPages;

    @Schema(description = "Primary genre", example = "Dystopian")
    private String genre;

    @Schema(description = "Short description of the book", example = "A dystopian social science fiction novel.")
    private String description;

    @Vectorize
    @Schema(description = "String use for vectorization")
    private String vectorize;

    @Schema(description = "List of genres or tags")
    private Set<String> genres;

    @Schema(description = "Additional metadata as key-value pairs")
    private Map<String, String> metadata;

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

    public Book isCheckedOut(boolean checkedOut) {
        this.checkedOut = checkedOut;
        return this;
    }

    public Book checkedOut(boolean checkedOut) {
        this.checkedOut = checkedOut;
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
        this.vectorize   = description;
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
