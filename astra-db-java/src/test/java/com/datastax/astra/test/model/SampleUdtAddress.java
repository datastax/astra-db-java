package com.datastax.astra.test.model;

import com.datastax.astra.client.tables.definition.types.TableUserDefinedType;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeField;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@TableUserDefinedType("udt_address")
@NoArgsConstructor
public class SampleUdtAddress {

    @TableUserDefinedTypeField(name ="street", type = TableUserDefinedTypeFieldTypes.TEXT)
    String street;

    @TableUserDefinedTypeField(name ="street", type = TableUserDefinedTypeFieldTypes.TEXT)
    String city;

    @TableUserDefinedTypeField(name ="state", type = TableUserDefinedTypeFieldTypes.TEXT)
    String state;

    @TableUserDefinedTypeField(name ="zip_code", type = TableUserDefinedTypeFieldTypes.INT)
    String zipCode;
}
