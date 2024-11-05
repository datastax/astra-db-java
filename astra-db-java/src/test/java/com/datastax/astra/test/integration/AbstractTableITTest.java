package com.datastax.astra.test.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTableITTest extends AbstractDataAPITest {

    record TableSimpleRow(String email, Boolean human, Integer age, String country, String name) {}

}