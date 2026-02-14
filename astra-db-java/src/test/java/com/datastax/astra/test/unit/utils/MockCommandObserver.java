package com.datastax.astra.test.unit.utils;

import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.command.ExecutionInfos;

import static org.assertj.core.api.Assertions.assertThat;


public class MockCommandObserver implements CommandObserver {

    @Override
    public void onCommand(ExecutionInfos executionInfo) {
        assertThat(executionInfo.getExecutionDate()).isNotNull();
        assertThat(executionInfo.getResponseHttpHeaders()).isNotNull();
    }
}
