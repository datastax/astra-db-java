package com.datastax.astra.test.model;

import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.EntityTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.INT;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.TEXT;

@Data
@EntityTable("table_composite_pk_annotated")
@NoArgsConstructor
@AllArgsConstructor
public class TableCompositeAnnotatedRow {

    @PartitionBy(0)
    @Column(name ="id", type=TEXT)
    private String idx;

    @PartitionBy(1)
    @Column(name ="name", type=TEXT)
    private String namex;

    @Column(name ="age", type=INT)
    private int agex;

}
