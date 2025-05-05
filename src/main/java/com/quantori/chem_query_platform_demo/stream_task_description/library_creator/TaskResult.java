package com.quantori.chem_query_platform_demo.stream_task_description.library_creator;

import com.quantori.cqp.api.model.Property;
import com.quantori.cqp.core.task.model.StreamTaskResult;

import java.util.Map;

public record TaskResult(Map<String, Property> aggregatedProperties) implements StreamTaskResult {
}
