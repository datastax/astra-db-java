package com.ibm.astra.demo.books;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dataset containing 30 sample books for testing and demonstration purposes.
 */
public class DataSet {

    public static final Book BOOK_1 = new Book()
            .title("The Midnight Library")
            .author("Matt Haig")
            .numberOfPages(304)
            .genres(Set.of("Fiction", "Fantasy", "Contemporary"))
            .description("A library between life and death where every book is a different life you could have lived")
            .metadata(Map.of("isbn", "978-0-525-55948-1", "language", "English", "edition", "First Edition"))
            .isCheckedOut(false);

    public static final Book BOOK_2 = new Book()
            .title("Project Hail Mary")
            .author("Andy Weir")
            .numberOfPages(496)
            .genres(Set.of("Science Fiction", "Adventure", "Thriller"))
            .description("A lone astronaut must save Earth from an extinction-level threat")
            .metadata(Map.of("isbn", "978-0-593-13520-5", "language", "English", "edition", "First Edition"))
            .isCheckedOut(true);

    public static final Book BOOK_3 = new Book()
            .title("The Seven Husbands of Evelyn Hugo")
            .author("Taylor Jenkins Reid")
            .numberOfPages(400)
            .genres(Set.of("Historical Fiction", "Romance", "Drama"))
            .description("A reclusive Hollywood icon reveals her scandalous life story")
            .metadata(Map.of("isbn", "978-1-501-16134-4", "language", "English", "edition", "Reprint"))
            .isCheckedOut(false);

    public static final Book BOOK_4 = new Book()
            .title("Atomic Habits")
            .author("James Clear")
            .numberOfPages(320)
            .genres(Set.of("Self-Help", "Psychology", "Business"))
            .description("An easy and proven way to build good habits and break bad ones")
            .metadata(Map.of("isbn", "978-0-735-21129-2", "language", "English", "edition", "First Edition"))
            .isCheckedOut(false);

    public static final Book BOOK_5 = new Book()
            .title("The Silent Patient")
            .author("Alex Michaelides")
            .numberOfPages(336)
            .genres(Set.of("Thriller", "Mystery", "Psychological"))
            .description("A woman shoots her husband and then never speaks another word")
            .metadata(Map.of("isbn", "978-1-250-30170-7", "language", "English", "edition", "First Edition"))
            .isCheckedOut(true);

    public static final Book BOOK_6 = new Book()
            .title("Educated")
            .author("Tara Westover")
            .numberOfPages(352)
            .genres(Set.of("Memoir", "Biography", "Non-Fiction"))
            .description("A memoir about a young woman who leaves her survivalist family to pursue education")
            .metadata(Map.of("isbn", "978-0-399-59050-4", "language", "English", "edition", "First Edition"))
            .isCheckedOut(false);

    public static final Book BOOK_7 = new Book()
            .title("The Song of Achilles")
            .author("Madeline Miller")
            .numberOfPages(378)
            .genres(Set.of("Historical Fiction", "Fantasy", "Romance"))
            .description("A retelling of the Iliad from Patroclus's perspective")
            .metadata(Map.of("isbn", "978-0-062-06032-6", "language", "English", "edition", "Reprint"))
            .isCheckedOut(false);

    public static final Book BOOK_8 = new Book()
            .title("Dune")
            .author("Frank Herbert")
            .numberOfPages(688)
            .genres(Set.of("Science Fiction", "Adventure", "Epic"))
            .description("A sweeping tale of politics, religion, and ecology on the desert planet Arrakis")
            .metadata(Map.of("isbn", "978-0-441-17271-9", "language", "English", "edition", "Anniversary Edition"))
            .isCheckedOut(true);

    public static final Book BOOK_9 = new Book()
            .title("Where the Crawdads Sing")
            .author("Delia Owens")
            .numberOfPages(384)
            .genres(Set.of("Mystery", "Romance", "Coming-of-Age"))
            .description("A murder mystery set in the marshlands of North Carolina")
            .metadata(Map.of("isbn", "978-0-735-21932-8", "language", "English", "edition", "First Edition"))
            .isCheckedOut(false);

    public static final Book BOOK_10 = new Book()
            .title("The Invisible Life of Addie LaRue")
            .author("V.E. Schwab")
            .numberOfPages(448)
            .genres(Set.of("Fantasy", "Historical Fiction", "Romance"))
            .description("A woman makes a Faustian bargain to live forever but be forgotten by everyone")
            .metadata(Map.of("isbn", "978-0-765-38750-0", "language", "English", "edition", "First Edition"))
            .isCheckedOut(false);

    public static final Book BOOK_11 = new Book()
            .title("Circe")
            .author("Madeline Miller")
            .numberOfPages(400)
            .genres(Set.of("Fantasy", "Mythology", "Historical Fiction"))
            .description("The story of the sorceress Circe from Greek mythology")
            .metadata(Map.of("isbn", "978-0-316-55633-0", "language", "English", "edition", "First Edition"))
            .isCheckedOut(true);

    public static final Book BOOK_12 = new Book()
            .title("The Martian")
            .author("Andy Weir")
            .numberOfPages(369)
            .genres(Set.of("Science Fiction", "Adventure", "Thriller"))
            .description("An astronaut is stranded on Mars and must survive using science and ingenuity")
            .metadata(Map.of("isbn", "978-0-553-41802-6", "language", "English", "edition", "First Edition"))
            .isCheckedOut(false);

    public static final Book BOOK_13 = new Book()
            .title("The Night Circus")
            .author("Erin Morgenstern")
            .numberOfPages(400)
            .genres(Set.of("Fantasy", "Romance", "Historical Fiction"))
            .description("Two young magicians compete in a mysterious circus that appears without warning")
            .metadata(Map.of("isbn", "978-0-307-74443-2", "language", "English", "edition", "First Edition"))
            .isCheckedOut(false);

    public static final Book BOOK_14 = new Book()
            .title("1984")
            .author("George Orwell")
            .numberOfPages(328)
            .genres(Set.of("Dystopian", "Science Fiction", "Political Fiction"))
            .description("A totalitarian regime controls every aspect of life in Oceania")
            .metadata(Map.of("isbn", "978-0-452-28423-4", "language", "English", "edition", "Centennial Edition"))
            .isCheckedOut(true);

    public static final Book BOOK_15 = new Book()
            .title("The Alchemist")
            .author("Paulo Coelho")
            .numberOfPages(208)
            .genres(Set.of("Fiction", "Philosophy", "Adventure"))
            .description("A shepherd boy's journey to find treasure and discover his personal legend")
            .metadata(Map.of("isbn", "978-0-061-12241-5", "language", "English", "edition", "25th Anniversary"))
            .isCheckedOut(false);

    public static final Book BOOK_16 = new Book()
            .title("The Hobbit")
            .author("J.R.R. Tolkien")
            .numberOfPages(310)
            .genres(Set.of("Fantasy", "Adventure", "Classic"))
            .description("Bilbo Baggins embarks on an unexpected journey with dwarves and a wizard")
            .metadata(Map.of("isbn", "978-0-547-92822-7", "language", "English", "edition", "75th Anniversary"))
            .isCheckedOut(false);

    public static final Book BOOK_17 = new Book()
            .title("The Book Thief")
            .author("Markus Zusak")
            .numberOfPages(552)
            .genres(Set.of("Historical Fiction", "War", "Coming-of-Age"))
            .description("Death narrates the story of a girl living in Nazi Germany who steals books")
            .metadata(Map.of("isbn", "978-0-375-84220-7", "language", "English", "edition", "10th Anniversary"))
            .isCheckedOut(true);

    public static final Book BOOK_18 = new Book()
            .title("Sapiens")
            .author("Yuval Noah Harari")
            .numberOfPages(464)
            .genres(Set.of("Non-Fiction", "History", "Science"))
            .description("A brief history of humankind from the Stone Age to the modern age")
            .metadata(Map.of("isbn", "978-0-062-31609-7", "language", "English", "edition", "First Edition"))
            .isCheckedOut(false);

    public static final Book BOOK_19 = new Book()
            .title("The Hunger Games")
            .author("Suzanne Collins")
            .numberOfPages(374)
            .genres(Set.of("Dystopian", "Science Fiction", "Young Adult"))
            .description("Teens fight to the death in a televised competition in a dystopian future")
            .metadata(Map.of("isbn", "978-0-439-02348-1", "language", "English", "edition", "First Edition"))
            .isCheckedOut(false);

    public static final Book BOOK_20 = new Book()
            .title("The Great Gatsby")
            .author("F. Scott Fitzgerald")
            .numberOfPages(180)
            .genres(Set.of("Classic", "Fiction", "Romance"))
            .description("The mysterious millionaire Jay Gatsby and his obsession with Daisy Buchanan")
            .metadata(Map.of("isbn", "978-0-743-27356-5", "language", "English", "edition", "Scribner"))
            .isCheckedOut(true);

    public static final Book BOOK_21 = new Book()
            .title("Harry Potter and the Sorcerer's Stone")
            .author("J.K. Rowling")
            .numberOfPages(309)
            .genres(Set.of("Fantasy", "Young Adult", "Adventure"))
            .description("A young wizard discovers his magical heritage and attends Hogwarts")
            .metadata(Map.of("isbn", "978-0-590-35340-3", "language", "English", "edition", "First American"))
            .isCheckedOut(false);

    public static final Book BOOK_22 = new Book()
            .title("To Kill a Mockingbird")
            .author("Harper Lee")
            .numberOfPages(324)
            .genres(Set.of("Classic", "Historical Fiction", "Coming-of-Age"))
            .description("A lawyer defends a black man accused of rape in 1930s Alabama")
            .metadata(Map.of("isbn", "978-0-061-12000-8", "language", "English", "edition", "50th Anniversary"))
            .isCheckedOut(false);

    public static final Book BOOK_23 = new Book()
            .title("The Catcher in the Rye")
            .author("J.D. Salinger")
            .numberOfPages(234)
            .genres(Set.of("Classic", "Coming-of-Age", "Fiction"))
            .description("Holden Caulfield's journey through New York City after being expelled")
            .metadata(Map.of("isbn", "978-0-316-76948-0", "language", "English", "edition", "Back Bay Books"))
            .isCheckedOut(true);

    public static final Book BOOK_24 = new Book()
            .title("Pride and Prejudice")
            .author("Jane Austen")
            .numberOfPages(432)
            .genres(Set.of("Classic", "Romance", "Historical Fiction"))
            .description("Elizabeth Bennet navigates love and society in Regency England")
            .metadata(Map.of("isbn", "978-0-141-43951-8", "language", "English", "edition", "Penguin Classics"))
            .isCheckedOut(false);

    public static final Book BOOK_25 = new Book()
            .title("The Lord of the Rings")
            .author("J.R.R. Tolkien")
            .numberOfPages(1178)
            .genres(Set.of("Fantasy", "Adventure", "Epic"))
            .description("Frodo Baggins must destroy the One Ring to save Middle-earth")
            .metadata(Map.of("isbn", "978-0-544-00341-5", "language", "English", "edition", "50th Anniversary"))
            .isCheckedOut(false);

    public static final Book BOOK_26 = new Book()
            .title("The Handmaid's Tale")
            .author("Margaret Atwood")
            .numberOfPages(311)
            .genres(Set.of("Dystopian", "Science Fiction", "Feminist"))
            .description("A woman's struggle for survival in a totalitarian theocracy")
            .metadata(Map.of("isbn", "978-0-385-49081-8", "language", "English", "edition", "Anchor Books"))
            .isCheckedOut(true);

    public static final Book BOOK_27 = new Book()
            .title("Brave New World")
            .author("Aldous Huxley")
            .numberOfPages(268)
            .genres(Set.of("Dystopian", "Science Fiction", "Classic"))
            .description("A futuristic society where humans are genetically engineered and conditioned")
            .metadata(Map.of("isbn", "978-0-060-85052-4", "language", "English", "edition", "Harper Perennial"))
            .isCheckedOut(false);

    public static final Book BOOK_28 = new Book()
            .title("The Kite Runner")
            .author("Khaled Hosseini")
            .numberOfPages(371)
            .genres(Set.of("Historical Fiction", "Drama", "Coming-of-Age"))
            .description("A story of friendship and redemption set in Afghanistan")
            .metadata(Map.of("isbn", "978-1-594-48000-3", "language", "English", "edition", "Riverhead Books"))
            .isCheckedOut(false);

    public static final Book BOOK_29 = new Book()
            .title("The Road")
            .author("Cormac McCarthy")
            .numberOfPages(287)
            .genres(Set.of("Post-Apocalyptic", "Fiction", "Drama"))
            .description("A father and son journey through a devastated America")
            .metadata(Map.of("isbn", "978-0-307-38789-9", "language", "English", "edition", "Vintage"))
            .isCheckedOut(true);

    public static final Book BOOK_30 = new Book()
            .title("Life of Pi")
            .author("Yann Martel")
            .numberOfPages(460)
            .genres(Set.of("Adventure", "Fantasy", "Philosophical"))
            .description("A boy survives 227 days at sea with a Bengal tiger")
            .metadata(Map.of("isbn", "978-0-156-02732-2", "language", "English", "edition", "Mariner Books"))
            .isCheckedOut(false);

    public static final List<Book> BOOKS = List.of(
            BOOK_1, BOOK_2, BOOK_3, BOOK_4, BOOK_5,
            BOOK_6, BOOK_7, BOOK_8, BOOK_9, BOOK_10,
            BOOK_11, BOOK_12, BOOK_13, BOOK_14, BOOK_15,
            BOOK_16, BOOK_17, BOOK_18, BOOK_19, BOOK_20,
            BOOK_21, BOOK_22, BOOK_23, BOOK_24, BOOK_25,
            BOOK_26, BOOK_27, BOOK_28, BOOK_29, BOOK_30
    );
}