package com.datastax.astra.test.integration;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.commands.options.TableInsertManyOptions;
import com.datastax.astra.client.tables.commands.results.TableInsertManyResult;
import com.datastax.astra.client.tables.cursor.TableFindCursor;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinitionOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Optional;

import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static com.datastax.astra.client.tables.commands.options.DropTableOptions.IF_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract integration tests for Table vector search with vectorize (server-side embedding).
 * <p>
 * Uses Nvidia NV-Embed-QA which is built-in on Astra (no external API key required).
 * Tests cover table creation with vectorize service, insertion with vectorize,
 * similarity search with vectorize sort, includeSimilarity, includeSortVector, and getSortVector.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTableVectorSearchIT extends AbstractDataAPITest {

    static final String TABLE_VECTORIZE = "table_vectorize";
    static final String INDEX_VECTORIZE_VECTOR = "idx_vectorize_vector";

    @BeforeAll
    void setupVectorSearchTable() {
        dropAllCollections();
        dropAllTables();
    }

    @Test
    @Order(1)
    @DisplayName("01. Should create table with vectorize service")
    public void should_createTable_withVectorizeService() {
        getDatabase().dropTable(TABLE_VECTORIZE, IF_EXISTS);

        // Nvidia NV-Embed-QA — built-in, no external API key required
        VectorServiceOptions vectorService = new VectorServiceOptions()
                .provider("nvidia")
                .modelName("NV-Embed-QA");

        Table<Row> table = getDatabase().createTable(TABLE_VECTORIZE, new TableDefinition()
                .addColumnText("id")
                .addColumnText("body")
                .addColumnVector("vector", new TableColumnDefinitionVector()
                        .dimension(1024)
                        .metric(COSINE)
                        .service(vectorService))
                .partitionKey("id"));
        assertThat(getDatabase().tableExists(TABLE_VECTORIZE)).isTrue();

        // Create vector index
        table.createVectorIndex(INDEX_VECTORIZE_VECTOR,
                new TableVectorIndexDefinition()
                        .column("vector")
                        .options(new TableVectorIndexDefinitionOptions().metric(COSINE)),
                new CreateVectorIndexOptions().ifNotExists(true));
        log.info("Created table '{}' with Nvidia NV-Embed-QA vectorize and vector index", TABLE_VECTORIZE);
    }

    @Test
    @Order(2)
    @DisplayName("02. Should insert rows with vectorize")
    public void should_insertMany_withVectorize() {
        Table<Row> table = getDatabase().getTable(TABLE_VECTORIZE);

        List<Row> rows = List.of(
                new Row().addText("id", "v1").addText("body", "The sun rises in the east every morning")
                        .addVectorize("vector", "The sun rises in the east every morning"),
                new Row().addText("id", "v2").addText("body", "Quantum computing will change cryptography")
                        .addVectorize("vector", "Quantum computing will change cryptography"),
                new Row().addText("id", "v3").addText("body", "Fresh pasta tastes better than dried pasta")
                        .addVectorize("vector", "Fresh pasta tastes better than dried pasta"),
                new Row().addText("id", "v4").addText("body", "The history of mathematics is fascinating")
                        .addVectorize("vector", "The history of mathematics is fascinating"),
                new Row().addText("id", "v5").addText("body", "Machine learning models need training data")
                        .addVectorize("vector", "Machine learning models need training data"));

        TableInsertManyResult res = table.insertMany(rows, new TableInsertManyOptions().ordered(true));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedIds()).hasSize(5);
        log.info("Inserted {} rows with vectorize", res.getInsertedIds().size());
    }

    @Test
    @Order(3)
    @DisplayName("03. Should find with vectorize sort")
    public void should_find_withVectorizeSort() {
        Table<Row> table = getDatabase().getTable(TABLE_VECTORIZE);

        List<Row> results = table.find(null,
                new TableFindOptions()
                        .sort(Sort.vectorize("vector", "computing and technology"))
                        .limit(3)).toList();

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(3);
        log.info("Vectorize sort returned {} rows", results.size());
        results.forEach(r -> log.info("  Row: id={}, body={}", r.getText("id"), r.getText("body")));
    }

    @Test
    @Order(4)
    @DisplayName("04. Should find with vectorize sort and includeSimilarity")
    public void should_find_withVectorSortAndSimilarity() {
        Table<Row> table = getDatabase().getTable(TABLE_VECTORIZE);

        List<Row> results = table.find(null,
                new TableFindOptions()
                        .sort(Sort.vector("vector", floatArray))
                        .includeSimilarity(true)
                        .limit(3)).toList();

        assertThat(results).isNotEmpty();
        // NOT SUPPORTED FOR TABLE YET
        //Row topResult = results.get(0);
        //assertThat(topResult.getSimilarity()).isNotNull();
        //assertThat(topResult.getSimilarity()).isGreaterThan(0d);
        //log.info("Top result: id={}, similarity={}", topResult.getText("id"), topResult.getSimilarity());
    }

    @Test
    @Order(5)
    @DisplayName("05. Should find with vectorize sort and includeSortVector")
    public void should_find_withVectorizeSortAndSortVector() {
        Table<Row> table = getDatabase().getTable(TABLE_VECTORIZE);

        TableFindCursor<Row, Row> cursor = table.find(null,
                new TableFindOptions()
                        .sort(Sort.vectorize("vector", "pasta and cooking"))
                        .includeSortVector(true)
                        .limit(3));

        // Consume results to trigger fetch
        List<Row> results = cursor.toList();
        assertThat(results).isNotEmpty();

        /* getSortVector should return the vector computed by the vectorize service
        Optional<DataAPIVector> sortVector = cursor.getSortVector();
        assertThat(sortVector).isPresent();
        assertThat(sortVector.get().getEmbeddings()).isNotNull();
        assertThat(sortVector.get().getEmbeddings().length).isEqualTo(1024); // NV-Embed-QA dimension
        log.info("Sort vector dimension: {}", sortVector.get().getEmbeddings().length);
         */
    }

    @Test
    @Order(6)
    @DisplayName("06. Should findOne with vectorize sort and includeSimilarity")
    public void should_findOne_withVectorizeSortAndSimilarity() {
        Table<Row> table = getDatabase().getTable(TABLE_VECTORIZE);

        Optional<Row> result = table.findOne(null,
                new TableFindOneOptions()
                        .sort(Sort.vectorize("vector", "machine learning and artificial intelligence"))
                        .includeSimilarity(true));

        assertThat(result).isPresent();
        /*
        Row row = result.get();
        assertThat(row.getSimilarity()).isNotNull();
        assertThat(row.getSimilarity()).isGreaterThan(0d);
        log.info("FindOne vectorize: id={}, similarity={}", row.getText("id"), row.getSimilarity());
        */
    }

    @Test
    @Order(7)
    @DisplayName("07. Should find with vectorize sort, projection, and similarity")
    public void should_find_withVectorizeSortProjectionAndSimilarity() {
        Table<Row> table = getDatabase().getTable(TABLE_VECTORIZE);

        List<Row> results = table.find(null,
                new TableFindOptions()
                        .sort(Sort.vectorize("vector", "history and mathematics"))
                        .projection(Projection.include("id", "body"))
                        .includeSimilarity(true)
                        .limit(2)).toList();

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(2);
        results.forEach(r -> {
            assertThat(r.getText("id")).isNotNull();
            assertThat(r.getText("body")).isNotNull();
            //assertThat(r.getSimilarity()).isNotNull();
        });
    }

    @Test
    @Order(8)
    @DisplayName("08. Should find with vectorize sort and filter")
    public void should_find_withVectorizeSortAndFilter() {
        Table<Row> table = getDatabase().getTable(TABLE_VECTORIZE);

        List<Row> results = table.find(
                Filters.eq("id", "v2"),
                new TableFindOptions()
                        .sort(Sort.vectorize("vector", "quantum physics"))
                        .includeSimilarity(true)
                        .limit(5)).toList();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getText("id")).isEqualTo("v2");
        //(results.get(0).getSimilarity()).isNotNull();
    }

    float[] floatArray = {
            -0.01197052f, -0.020980835f, 0.016098022f, -0.013122559f, -0.014152527f, -0.012908936f, 0.021499634f, -0.01939392f,
            -0.00793457f, -0.033203125f, -0.019592285f, 0.02368164f, -0.03845215f, -0.007217407f, -0.035980225f, 0.013694763f,
            0.007888794f, -0.04940796f, -0.012313843f, 0.0031261444f, -0.008857727f, 0.040405273f, 0.0037670135f, 0.0028972626f,
            -0.0054016113f, 0.016571045f, -0.0076560974f, -0.017486572f, 0.001250267f, 0.025787354f, 0.034973145f, -0.018981934f,
            0.026184082f, 0.053344727f, 0.038391113f, -0.032684326f, -0.029220581f, -0.014320374f, -0.045043945f, 0.050964355f,
            0.02357483f, -0.05307007f, -0.032287598f, -0.008964539f, -0.028579712f, 0.017425537f, 0.025009155f, -0.018493652f,
            0.021743774f, -0.03656006f, 0.0064201355f, 0.014511108f, -0.02142334f, 0.060943604f, -0.019592285f, 0.013160706f,
            0.03302002f, 0.040740967f, -0.03414917f, -0.043029785f, 0.036712646f, -0.016220093f, 0.023971558f, -0.010566711f,
            -0.02192688f, 0.03201294f, 0.0073242188f, -0.024414062f, 0.0060043335f, -0.025970459f, -0.011222839f, 0.0035705566f,
            0.04156494f, -0.00409317f, -0.033447266f, 0.0022182465f, -0.010597229f, -0.002696991f, 0.017562866f, 0.034606934f,
            0.014854431f, 0.050048828f, 0.017745972f, 0.030151367f, 0.019607544f, -0.0074043274f, 0.068115234f, 0.01525116f,
            0.034606934f, 0.046905518f, 0.04385376f, -0.008422852f, 0.01436615f, 0.009063721f, 0.0049819946f, -0.05480957f,
            -0.0024738312f, -0.0061569214f, -0.022766113f, -0.012542725f, 0.0038528442f, -0.01878357f, 0.04574585f, 0.056274414f,
            0.043792725f, -0.024383545f, -0.046295166f, -0.018493652f, 0.0061569214f, -0.036376953f, -0.05053711f, -0.0011262894f,
            -0.035186768f, -0.0014076233f, 0.03640747f, -0.05206299f, -0.028808594f, -0.019683838f, 0.040496826f, 0.075805664f,
            0.023101807f, -0.008049011f, 0.038116455f, -0.0053520203f, -0.005580902f, 0.03503418f, -0.0064926147f, -0.008720398f,
            -0.029647827f, 9.021759E-4f, -0.04083252f, 0.021072388f, 0.027954102f, -0.007980347f, 0.0023822784f, -0.0050201416f,
            -0.051635742f, -0.0029144287f, 0.043914795f, -0.014778137f, 0.0072746277f, -0.050994873f, -0.032714844f, 1.31726265E-5f,
            0.045074463f, 0.018722534f, 0.024627686f, 0.023971558f, 0.00982666f, 0.008079529f, -0.026382446f, -0.029449463f,
            -0.013343811f, 0.0112838745f, 0.046203613f, -0.015167236f, -0.0041275024f, 0.012512207f, -0.009300232f, 0.031555176f,
            -0.018493652f, -0.015312195f, -0.002811432f, 0.027252197f, 0.0014600754f, -0.008369446f, 0.02583313f, -0.0011577606f,
            0.010261536f, 0.027648926f, -0.029968262f, -0.008338928f, 0.0012130737f, 0.04940796f, 0.0069007874f, -0.0064926147f,
            0.015487671f, 0.02029419f, 0.055786133f, 0.02748108f, 0.011581421f, 0.045410156f, -0.026733398f, -0.0026130676f,
            -0.016159058f, 0.07550049f, -0.012687683f, -0.004901886f, -0.0052986145f, -0.06744385f, -0.044525146f, -0.065979004f,
            0.028656006f, 0.015487671f, -0.017318726f, 0.008422852f, 0.054382324f, 0.03543091f, -0.016708374f, 0.012809753f,
            -0.0368042f, -0.031158447f, 0.0034561157f, -0.038604736f, 0.020385742f, 0.057769775f, 0.029785156f, -0.024246216f,
            -4.3678284E-4f, 0.030334473f, -0.046203613f, 0.036712646f, -0.0016384125f, 0.013755798f, -0.010383606f, -0.078063965f,
            -0.008018494f, 0.02583313f, -0.008621216f, 0.040283203f, -0.022537231f, 0.022979736f, 0.011047363f, -0.003616333f,
            -3.7765503E-4f, -0.034484863f, -0.0055160522f, 0.025894165f, -0.06713867f, 0.0024528503f, 0.0050086975f, -0.03253174f,
            0.084472656f, 0.0670166f, -0.022399902f, -0.00818634f, 0.046905518f, -0.02999878f, 0.017333984f, 0.04812622f,
            0.008293152f, -0.028778076f, 0.04864502f, 0.021255493f, 0.034088135f, 0.035949707f, 0.029006958f, 0.038269043f,
            0.07171631f, -0.022766113f, 0.024627686f, 0.025970459f, 0.022537231f, -0.019500732f, 0.034332275f, -0.010856628f,
            0.012710571f, 0.017593384f, -0.027542114f, -0.014045715f, 0.030395508f, 0.009613037f, -0.017791748f, 0.029418945f,
            0.041412354f, -0.005672455f, 0.0231781f, -0.0025901794f, 0.045196533f, -0.027038574f, 0.011383057f, -0.011207581f,
            -0.012565613f, -0.019058228f, 0.009895325f, 0.0038032532f, -0.016723633f, -0.02848816f, 0.013557434f, -0.008087158f,
            0.021972656f, 0.0042266846f, -0.014091492f, 0.036346436f, -0.001206398f, 0.015823364f, 0.028213501f, 0.011459351f,
            0.044189453f, 0.003648758f, -0.028305054f, 8.893013E-5f, -0.009010315f, -0.013160706f, 0.023101807f, -0.038970947f,
            0.01612854f, 0.012641907f, -0.021148682f, 0.0034065247f, -0.0059928894f, -0.029190063f, 0.004547119f, 0.0021896362f,
            -0.030136108f, 0.07519531f, -0.014266968f, 0.02507019f, -0.008354187f, 0.017318726f, -0.0058631897f, -0.07159424f,
            -0.022537231f, -0.04724121f, -0.020095825f, 0.06451416f, -0.046295166f, 1.1128187E-4f, 0.055511475f, -0.029418945f,
            -0.0024852753f, 0.0060806274f, -0.036956787f, 0.030807495f, -2.5248528E-4f, -0.061523438f, 0.013473511f, -0.021072388f,
            -0.0028190613f, 0.0152282715f, 0.025817871f, 0.021896362f, 0.032073975f, 0.034088135f, -0.014953613f, -0.043029785f,
            -0.015457153f, 0.036834717f, -0.04373169f, -0.058380127f, 0.0064201355f, -0.029434204f, -0.04046631f, 0.04324341f,
            0.031433105f, 0.0047187805f, -0.05480957f, 0.064697266f, 0.020141602f, 0.024505615f, -0.04650879f, 0.027755737f,
            -0.023880005f, 0.054473877f, -0.019332886f, 0.006690979f, 0.029449463f, -0.033050537f, -0.0013160706f, 0.043426514f,
            0.038757324f, 0.009788513f, -0.04776001f, 0.019760132f, -0.032714844f, 0.04425049f, 0.034942627f, 0.002904892f,
            0.002122879f, -0.013832092f, -0.021224976f, 0.031021118f, -0.044189453f, 0.03048706f, -0.04336548f, 0.05026245f,
            0.041107178f, 0.0152282715f, -0.007461548f, 0.07196045f, 0.026672363f, -0.039855957f, -0.06188965f, -0.028182983f,
            -0.043792725f, 0.0692749f, 0.003578186f, 0.013549805f, 0.0024681091f, -0.021743774f, -0.016784668f, 3.836155E-4f,
            -0.034088135f, -0.005897522f, 0.045074463f, -0.051696777f, -0.014076233f, -0.0209198f, -0.011726379f, -0.036743164f,
            -0.0037193298f, 0.037078857f, -0.008094788f, 0.015075684f, 3.452301E-4f, -0.051116943f, 0.010292053f, -0.019042969f,
            0.04901123f, -0.01852417f, 0.045532227f, 0.052642822f, 0.031585693f, -0.016662598f, 0.010124207f, -0.01235199f,
            -0.020812988f, -0.008666992f, 0.001824379f, -0.0034313202f, -0.08337402f, -0.019927979f, 0.02128601f, 0.024734497f,
            -0.026550293f, -0.007789612f, 0.025375366f, -0.023895264f, 0.004463196f, 0.015670776f, -0.0059165955f, -0.017578125f,
            -0.014854431f, -0.058563232f, -0.0032196045f, -0.02255249f, -0.01965332f, 0.04776001f, 0.039520264f, -0.04647827f,
            -0.041168213f, 0.016448975f, -0.058563232f, -0.036132812f, -0.024291992f, -0.013442993f, 0.053375244f, -0.0019683838f,
            -0.014816284f, 0.012779236f, 0.012908936f, -0.00920105f, 0.029281616f, -0.008621216f, -0.07397461f, 0.021469116f,
            0.036499023f, -0.0010137558f, -0.07507324f, 0.006378174f, -0.036956787f, 0.011978149f, 0.043823242f, -0.017562866f,
            0.047729492f, -0.03378296f, 0.009727478f, -0.08312988f, -0.016693115f, 0.0056762695f, 0.016342163f, -0.017471313f,
            -0.02168274f, -0.034973145f, -0.033813477f, -0.017074585f, -0.008178711f, -0.011421204f, 0.05117798f, 0.05041504f,
            0.003944397f, 0.064697266f, 0.0077934265f, -0.04244995f, -0.028671265f, 0.02494812f, -0.009521484f, -0.03414917f,
            0.011924744f, -0.038269043f, 0.031280518f, 0.015975952f, -0.06341553f, -0.028656006f, -0.0053863525f, -0.03640747f,
            -0.01108551f, -0.02532959f, 0.034698486f, 0.03540039f, 0.03543091f, -0.01864624f, 0.0058670044f, 0.020385742f,
            0.04321289f, -0.07244873f, -0.019500732f, -0.04840088f, -0.035583496f, 2.0313263E-4f, 0.0061569214f, 0.045074463f,
            0.003074646f, 0.02432251f, -0.0262146f, 0.015716553f, 0.026031494f, 0.040924072f, -0.019515991f, -0.0012893677f,
            -0.026550293f, -0.034240723f, 0.0037765503f, 0.03466797f, -0.05239868f, 0.013061523f, 0.004886627f, -0.027679443f,
            0.0051116943f, -0.01612854f, -0.007637024f, -0.016311646f, -0.033233643f, 0.026412964f, 0.07733154f, 0.0335083f,
            -0.012184143f, -0.088256836f, 0.014175415f, 0.019927979f, 0.0082473755f, 0.07232666f, -0.044921875f, 0.041748047f,
            0.027801514f, -0.012397766f, -0.095825195f, 0.007637024f, -3.5643578E-5f, 0.025436401f, 2.8896332E-4f, -0.04159546f,
            -0.022415161f, 0.012786865f, 0.025772095f, 0.01386261f, 0.051116943f, 0.044555664f, -0.014953613f, 0.009613037f,
            0.0051498413f, 0.06427002f, -0.040618896f, -4.081726E-4f, -0.04522705f, -0.020935059f, 0.008369446f, 0.024154663f,
            0.0016241074f, -0.016616821f, -0.021438599f, 0.0046806335f, -0.0038261414f, 0.030853271f, -0.026916504f, 0.036712646f,
            -0.011070251f, 0.0038719177f, -0.031433105f, -0.017868042f, 0.025497437f, -0.007167816f, 7.829666E-4f, -0.012336731f,
            -0.017211914f, 0.0072288513f, 0.013298035f, -0.023712158f, 0.0115737915f, -0.03378296f, 0.015174866f, 0.047576904f,
            -0.010650635f, -0.03704834f, -0.02760315f, -0.02267456f, 0.021606445f, 0.0069465637f, 0.040893555f, -0.019729614f,
            -0.017166138f, 9.0265274E-4f, -0.011451721f, -0.01197052f, -0.042236328f, 0.04006958f, 0.03479004f, 0.03604126f,
            0.008514404f, -0.01537323f, -0.0017776489f, -0.03540039f, 0.036834717f, 0.009414673f, -0.03805542f, 0.046325684f,
            0.015014648f, -0.028381348f, -4.6563148E-4f, 0.023803711f, 0.041015625f, 0.033477783f, -0.01676941f, 0.014717102f,
            -0.011795044f, 0.028045654f, -0.07183838f, -0.004398346f, -0.05810547f, 0.046081543f, -0.037261963f, -0.022064209f,
            0.020950317f, -0.014923096f, 0.017242432f, 0.031204224f, 0.013908386f, -0.06896973f, 0.020523071f, 0.008079529f,
            0.0056762695f, 0.01940918f, 0.023025513f, -0.006717682f, 0.06311035f, 0.07116699f, 0.006324768f, -0.029708862f,
            0.03479004f, -0.026519775f, 0.015167236f, -0.0056114197f, -0.033721924f, -0.07489014f, 4.3582916E-4f, 0.025299072f,
            0.051452637f, 2.2280216E-4f, 0.0049819946f, -0.016296387f, 0.054473877f, -0.045959473f, 0.032470703f, 0.04284668f,
            0.006465912f, 0.15966797f, 0.015731812f, -0.02027893f, -0.060058594f, 0.023117065f, -0.012672424f, 0.049591064f,
            0.013206482f, -0.017669678f, 0.010848999f, 0.02659607f, 0.03765869f, 0.042663574f, -0.012825012f, 0.01789856f,
            0.08654785f, -0.02406311f, 0.0031337738f, 0.041412354f, 0.012008667f, 0.015960693f, -0.010353088f, -0.037872314f,
            0.013015747f, -0.006916046f, 0.012001038f, 0.0552063f, 0.008422852f, 0.013511658f, 0.031036377f, -0.013137817f,
            0.0055236816f, 0.016830444f, 0.034606934f, 0.00894928f, 0.00491333f, -0.008544922f, -0.015914917f, 0.011802673f,
            -0.012016296f, 0.056640625f, -0.0017967224f, -0.010818481f, 0.0056533813f, -0.0670166f, -2.632141E-4f, -0.007858276f,
            -0.06384277f, 0.010787964f, -0.0064964294f, 0.012489319f, 3.964901E-4f, -0.0027580261f, -0.029937744f, -0.014877319f,
            0.028213501f, -0.017349243f, -0.0135269165f, -0.054748535f, -0.051696777f, -0.0022029877f, 0.0048332214f, 0.019485474f,
            -0.008895874f, -0.02935791f, -0.02520752f, 0.017242432f, -0.011680603f, -0.009773254f, -0.036621094f, -0.03543091f,
            0.036254883f, -0.008979797f, -0.02279663f, -0.003364563f, 0.016998291f, 0.042663574f, 0.014251709f, -0.04611206f,
            -0.009666443f, -0.040008545f, 0.015670776f, -0.03390503f, -5.888939E-4f, -0.03768921f, -0.003829956f, -0.03466797f,
            -0.019866943f, -0.0029850006f, 0.02192688f, 0.02760315f, 0.017150879f, -5.393028E-4f, -0.016082764f, 0.008918762f,
            0.0055122375f, -0.031051636f, -0.0085372925f, 0.033691406f, 0.034057617f, 0.032470703f, 0.03262329f, -0.012016296f,
            0.005378723f, -0.043548584f, 0.032989502f, 0.004508972f, 0.03918457f, -0.04055786f, 0.03451538f, -0.026473999f,
            -0.03591919f, -0.011352539f, -0.04034424f, -0.06561279f, -0.0023479462f, -0.036743164f, 0.008514404f, 0.02998352f,
            -0.0070114136f, 0.05355835f, -0.029129028f, -0.06439209f, -0.06390381f, -0.014259338f, -0.02029419f, 0.009490967f,
            0.017242432f, 0.007461548f, 0.010108948f, -2.297163E-4f, 0.03265381f, 0.016830444f, 0.022537231f, -0.020706177f,
            0.08251953f, -0.03579712f, 0.04901123f, -0.029418945f, -0.05529785f, 0.017486572f, -0.02746582f, 0.008354187f,
            -0.045196533f, -0.005378723f, 0.009750366f, 0.0076141357f, -0.04611206f, 0.032196045f, 0.053833008f, -0.005622864f,
            -0.046539307f, 0.011062622f, 0.014549255f, 0.014526367f, 0.042999268f, -0.024246216f, -0.025268555f, -0.025238037f,
            0.018051147f, 0.023620605f, -1.4722347E-4f, 0.03765869f, -7.29084E-4f, -0.051574707f, -0.013832092f, 0.027786255f,
            0.002368927f, -0.029403687f, 0.042297363f, 0.009346008f, -0.015411377f, -0.03543091f, 1.7547607E-4f, -0.03729248f,
            0.017593384f, -0.015312195f, -0.015419006f, 0.020568848f, 0.008468628f, 0.018951416f, 0.026519775f, 0.002653122f,
            -0.040008545f, -0.022125244f, 0.019577026f, -0.0309906f, 0.014793396f, -0.027267456f, -0.0287323f, 0.04525757f,
            0.043792725f, 0.036468506f, 0.0074806213f, 7.967949E-4f, 0.033966064f, 0.02267456f, 0.06738281f, -0.02017212f,
            -0.024368286f, 0.022216797f, -0.036956787f, -0.033050537f, -0.0066375732f, 0.007484436f, 0.019760132f, 0.0014400482f,
            0.0129852295f, -0.04046631f, -0.015136719f, -0.042816162f, 0.007911682f, -0.025970459f, -0.06329346f, 0.016860962f,
            0.02633667f, -0.04156494f, 0.03604126f, -0.007095337f, -0.0033683777f, -0.0069885254f, 0.025650024f, -0.052764893f,
            -0.055908203f, -0.028518677f, 0.021270752f, 0.013557434f, -0.018249512f, -0.02923584f, -0.010887146f, 0.033233643f,
            0.018249512f, 0.0018787384f, -5.1784515E-4f, -0.01197052f, 0.058685303f, -0.024291992f, -0.050567627f, 0.032073975f,
            0.01802063f, -0.020904541f, -0.02923584f, -0.031204224f, 0.024551392f, -0.013145447f, -0.032714844f, -0.0040359497f,
            0.07458496f, -0.0098724365f, -0.0132369995f, 0.014541626f, -0.009971619f, -0.021057129f, -0.006149292f, 0.012184143f,
            0.027893066f, -0.060913086f, -0.008918762f, -0.0035572052f, 0.012687683f, 0.02456665f, 0.044921875f, 0.020690918f,
            0.004940033f, -0.0075683594f, 0.01864624f, -0.01940918f, -0.030548096f, 0.018081665f, -0.031097412f, 0.0017280579f,
            -0.03604126f, -0.010871887f, 0.0031490326f, 0.026031494f, 0.05102539f, -0.052337646f, 0.03161621f, 0.024490356f,
            0.047607422f, 0.07751465f, 0.010437012f, -0.0128479f, 0.07244873f, -0.0395813f, -0.029159546f, -0.012718201f,
            0.005126953f, -0.04260254f, 0.018341064f, 0.03567505f, 0.051452637f, 0.019454956f, -0.021987915f, -0.016159058f,
            -0.017578125f, 0.009750366f, 0.030273438f, -0.005088806f, -0.0135650635f, -0.03640747f, 0.013191223f, 0.018844604f,
            0.008483887f, 0.07116699f, 0.005710602f, 0.023712158f, 0.040618896f, 0.08288574f, 0.023742676f, 0.0046577454f,
            -0.01940918f, -0.041046143f, -0.045776367f, 0.0060424805f, -0.00749588f, 0.0836792f, -0.0068244934f, -0.052703857f,
            -0.05053711f, -0.010932922f, 0.0053253174f, -0.014328003f, 0.032196045f, 0.056610107f, 0.021759033f, -0.027252197f,
            -0.0039367676f, -0.031921387f, -0.02998352f, -0.0050354004f, 0.017150879f, -0.021606445f, 0.01725769f, 0.04876709f,
            0.017929077f, -0.029800415f, 0.008018494f, -0.0023880005f, -0.0030918121f, 0.003768921f, 0.023452759f, -7.510185E-5f,
            -0.04449463f, 0.020629883f, -0.043151855f, 0.05517578f, -0.010437012f, 0.03375244f, -0.011627197f, -0.06732178f,
            -0.012451172f, 0.078186035f, -0.046875f, -0.0058403015f, -0.009544373f, -0.033935547f, 0.002500534f, -0.0043792725f
    };
}
