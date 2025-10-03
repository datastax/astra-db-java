package com.datastax.astra.test.integration.demo.dto;

import com.datastax.astra.client.tables.definition.types.TableUserDefinedType;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeField;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableUserDefinedType("person")
@NoArgsConstructor
@AllArgsConstructor
public class PersonUdtEntity {

    @TableUserDefinedTypeField(name = "user_name", type = TableUserDefinedTypeFieldTypes.TEXT)
    private String userName;

    @TableUserDefinedTypeField(name = "age", type = TableUserDefinedTypeFieldTypes.INT)
    private Integer age;

}
