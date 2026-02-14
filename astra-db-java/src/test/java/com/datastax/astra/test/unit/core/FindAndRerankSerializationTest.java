package com.datastax.astra.test.unit.core;

import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class FindAndRerankSerializationTest {

    static final DocumentSerializer SERIALIZER = new DocumentSerializer();

    @Test
    public void should_serialize_hybrid() {
        // INSERTING - SAME TEXTS
        // {
        //	 "_id": "1"
        //	 "$hybrid" : "vectorize and bm25 this text pls",
        // }
        Document doc1 =  new Document(1).hybrid(new Hybrid("text"));
        Assertions.assertEquals("{\"_id\":1,\"$hybrid\":\"text\"}",
        //Assertions.assertEquals("{\"_id\":1,\"$hybrid\":{\"$vectorize\":\"text\",\"$lexical\":\"text\"}}",
                SERIALIZER.marshall(doc1));

        //
        // {
        //   "_id": "1"
        //   "$vectorize" : "vectorize and bm25 this text pls",
        //   "$lexical" : "vectorize and bm25 this text pls",
        // }
        Document doc2 = new Document(2)
                .vectorize("vvv")
                .lexical("lll");
        Assertions.assertEquals("{\"_id\":2,\"$vectorize\":\"vvv\",\"$lexical\":\"lll\"}",
                SERIALIZER.marshall(doc2));

        // INSERTING - DIFFERENT TEXTS
        // $hybrid as an object, that sets both the $vectorize and $lexical as diff fields
        // {
        //   "_id": "1"
        //   "$hybrid": {
        //      "$vectorize": "i like cheese",
        //      "$lexical" : "cheese"
        //    }
        // }
        Document doc3 =  new Document(3).hybrid(new Hybrid().vectorize("vvv").lexical("lll"));
        Assertions.assertEquals("{\"_id\":3,\"$hybrid\":{\"$vectorize\":\"vvv\",\"$lexical\":\"lll\"}}",
                SERIALIZER.marshall(doc3));
    }

}
