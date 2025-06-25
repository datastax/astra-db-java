package com.datastax.astra.client.tables;

import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.PartitionSort;

import java.time.Instant;
import java.util.UUID;

public class AaronSample {

    @EntityTable("latest_videos")
    public static class LatestVideoTableEntity {

        @PartitionBy(0)
        @Column(name = "yyyymmdd")
        private String yyyymmdd;

        @PartitionSort(position = 1, order = SortOrder.DESCENDING)
        @Column(name = "added_date")
        private Instant addedDate;

        @PartitionSort(position = 2, order = SortOrder.ASCENDING)
        @Column(name = "videoid")
        private UUID videoId;

        @Column(name = "name")
        private String name;

        @Column(name = "preview_image_location")
        private String previewImageLocation;

        @Column(name = "userid")
        private UUID userId;
    }
}
