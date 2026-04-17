package com.ibm.astra.demo.books;

import com.datastax.astra.client.core.query.Filters;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return StreamSupport.stream(bookRepository.findAll().spliterator(), false)
                .toList();
    }

    public Optional<Book> findById(String id) {
        return bookRepository.findById(id);
    }

    public Book create(Book book) {
        validateBook(book);
        if (book.getId() == null || book.getId().isBlank()) {
            throw new IllegalArgumentException("Book id must be provided");
        }
        if (bookRepository.existsById(book.getId())) {
            throw new IllegalArgumentException("Book with id '%s' already exists".formatted(book.getId()));
        }
        return bookRepository.save(enrichBook(book));
    }

    public void flush() {
        bookRepository.deleteAll();
    }

    public Book update(String id, Book book) {
        validateBook(book);
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book with id '%s' was not found".formatted(id)));

        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setCheckedOut(book.isCheckedOut());
        existing.setNumberOfPages(book.getNumberOfPages());
        existing.setGenre(book.getGenre());
        existing.setDescription(book.getDescription());
        existing.setGenres(book.getGenres());
        existing.setMetadata(book.getMetadata());

        bookRepository.getCollection().replaceOne(Filters.id(id), enrichBook(existing));
        return bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book with id '%s' was not found after update".formatted(id)));
    }

    public void delete(String id) {
        if (!bookRepository.existsById(id)) {
            throw new NoSuchElementException("Book with id '%s' was not found".formatted(id));
        }
        bookRepository.deleteById(id);
    }

    public int loadDefaultDataSet() {
        return insertMany(DataSet.BOOKS);
    }

    public int insertMany(List<Book> books) {
        if (books == null || books.isEmpty()) {
            return 0;
        }
        List<Book> booksToInsert = books.stream()
                .map(this::enrichBook)
                .filter(book -> book.getId() != null && !book.getId().isBlank())
                .filter(book -> !bookRepository.existsById(book.getId()))
                .toList();

        if (!booksToInsert.isEmpty()) {
            bookRepository.saveAll(booksToInsert);
        }
        return booksToInsert.size();
    }

    public List<Book> searchBooks(BookVectorSearchRequest query) {
        return bookRepository.search(query.getQuery(), query.getLimit());
    }

    private void validateBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book payload must be provided");
        }
        if (book.getTitle() == null || book.getTitle().isBlank()) {
            throw new IllegalArgumentException("Book title must be provided");
        }
        if (book.getAuthor() == null || book.getAuthor().isBlank()) {
            throw new IllegalArgumentException("Book author must be provided");
        }
    }

    private Book enrichBook(Book book) {
        if (book.getId() == null || book.getId().isBlank()) {
            book.setId(slugify(book.getTitle(), book.getAuthor()));
        }
        book.setDescription(defaultString(book.getDescription()));
        return book;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String slugify(String title, String author) {
        return (title + "-" + author)
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

}
