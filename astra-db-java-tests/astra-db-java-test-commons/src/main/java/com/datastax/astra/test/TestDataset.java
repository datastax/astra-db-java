package com.datastax.astra.test;

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.definition.documents.types.ObjectId;
import com.datastax.astra.test.model.ProductString;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestDataset {

    public static String COLLECTION_SIMPLE    = "collection_simple";
    public static String COLLECTION_OBJECT_ID = "collection_objectid";
    public static String COLLECTION_UUID      = "collection_uuid";
    public static String COLLECTION_UUID_V6   = "collection_uuidv6";
    public static String COLLECTION_UUID_V7   = "collection_uuidv7";
    public static String COLLECTION_VECTOR    = "collection_vector";
    public static String COLLECTION_DENY      = "collection_deny";
    public static String COLLECTION_ALLOW     = "collection_allow";

    public static final String TABLE_SIMPLE       = "table_simple";
    public static final String TABLE_COMPOSITE    = "table_composite_pk";
    public static final String TABLE_TYPES        = "table_types";
    public static final String TABLE_CASSIO       = "table_cassio";
    public static final String TABLE_ALL_RETURNS  = "table_all_returns";

    public static final List<Document> DOCS_SONG_DIRE_STRAITS = List.of(
            new Document(1).vectorize("A lovestruck Romeo sings the streets a serenade"),
            new Document(2).vectorize("Finds a streetlight, steps out of the shade"),
            new Document(3).vectorize("Says something like, You and me babe, how about it?"),
            new Document(4).vectorize("Juliet says,Hey, it's Romeo, you nearly gimme a heart attack"),
            new Document(5).vectorize("He's underneath the window"),
            new Document(6).vectorize("She's singing, Hey la, my boyfriend's back"),
            new Document(7).vectorize("You shouldn't come around here singing up at people like that"),
            new Document(8).vectorize("Anyway, what you gonna do about it?")
    );


    public static Document COMPLETE_DOCUMENT = new Document().id("1")
            .append("metadata_instant", Instant.now())
            .append("metadata_date", new Date())
            .append("metadata_calendar", Calendar.getInstance())
            .append("metadata_int", 1)
            .append("metadata_objectId", new ObjectId())
            .append("metadata_long", 1232123323L)
            .append("metadata_double", 1213.343243d)
            .append("metadata_float", 1.1232434543f)
            .append("metadata_string", "hello")
            .append("metadata_short", Short.valueOf("1"))
            .append("metadata_string_array", new String[]{"a", "b", "c"})
            .append("metadata_int_array", new Integer[]{1, 2, 3})
            .append("metadata_long_array", new Long[]{1L, 2L, 3L})
            .append("metadata_double_array", new Double[]{1d, 2d, 3d})
            .append("metadata_float_array", new Float[]{1f, 2f, 3f})
            .append("metadata_short_array", new Short[]{1, 2, 3})
            .append("metadata_boolean", true)
            .append("metadata_boolean_array", new Boolean[]{true, false, true})
            .append("metadata_uuid", UUID.randomUUID())
            .append("metadata_uuid_array", new UUID[]{UUID.randomUUID(), UUID.randomUUID()})
            .append("metadata_map", Map.of("key1", "value1", "key2", "value2"))
            .append("metadata_list", List.of("value1", "value2"))
            .append("metadata_byte", Byte.valueOf("1"))
            .append("metadata_character", 'c')
            .append("metadata_enum", AstraDBAdmin.FREE_TIER_CLOUD)
            .append("metadata_enum_array", new CloudProviderType[]{AstraDBAdmin.FREE_TIER_CLOUD, CloudProviderType.AWS})
            .append("metadata_object", new ProductString("p1", "name", 10.1));

}
