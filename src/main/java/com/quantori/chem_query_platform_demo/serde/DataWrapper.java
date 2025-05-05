package com.quantori.chem_query_platform_demo.serde;

import com.quantori.cqp.core.task.model.DataProvider;

public record DataWrapper<T>(T data) implements DataProvider.Data {
}
