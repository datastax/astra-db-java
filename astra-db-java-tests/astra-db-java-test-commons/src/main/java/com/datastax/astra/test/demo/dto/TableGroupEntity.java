package com.datastax.astra.test.demo.dto;

import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@EntityTable("table_group")
public class TableGroupEntity {

    @PartitionBy(0)
    @Column(name = "id", type = TableColumnTypes.UUID)
    private UUID id;

    @Column(name = "group_leader", type = TableColumnTypes.USERDEFINED, udtName = "person")
    private PersonUdtEntity groupLeader;

    @Column(
            name = "group_members",
            type = TableColumnTypes.SET,
            valueType = TableColumnTypes.USERDEFINED,
            udtName = "person")
    private Set<PersonUdtEntity> groupMembers;

    @Column(
            name = "group_roles",
            type = TableColumnTypes.MAP,
            keyType = TableColumnTypes.TEXT,
            valueType = TableColumnTypes.USERDEFINED,
            udtName = "person")
    private Map<String, PersonUdtEntity> groupRoles;
}
