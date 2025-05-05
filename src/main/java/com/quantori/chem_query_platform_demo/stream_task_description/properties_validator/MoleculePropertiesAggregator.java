package com.quantori.chem_query_platform_demo.stream_task_description.properties_validator;

import com.quantori.chem_query_platform_demo.stream_task_description.library_creator.TaskResult;
import com.quantori.chem_query_platform_demo.serde.DataWrapper;
import com.quantori.cqp.api.model.Property;
import com.quantori.cqp.core.task.model.DataProvider;
import com.quantori.cqp.core.task.model.ResultAggregator;
import com.quantori.cqp.core.task.model.StreamTaskResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor
public class MoleculePropertiesAggregator implements ResultAggregator {

    private final Map<String, Property> aggregatedProperties = new ConcurrentHashMap<>();

    @Override
    public void consume(DataProvider.Data data) {
        List<Property> incomingProperties = ((DataWrapper<List<Property>>) data).data();
        mergeProperties(aggregatedProperties, incomingProperties);
    }

    @Override
    public StreamTaskResult getResult() {
        return new TaskResult(aggregatedProperties);
    }

    @Override
    public double getPercent() {
        return 0;
    }

    @Override
    public void taskCompleted(boolean successful) {
        defaultNullPropertyTypes(aggregatedProperties);
    }

    private void mergeProperties(Map<String, Property> resultMap, List<Property> newProperties) {
        newProperties.forEach(newProp -> {
            String propName = newProp.getName();
            Property.PropertyType newType = newProp.getType();
            resultMap.compute(propName, (key, existingProp) -> {
                if (existingProp == null) {
                    return newProp;
                }
                Property.PropertyType resolvedType = resolvePropertyType(existingProp.getType(), newType);
                newProp.setType(resolvedType);
                return newProp;
            });
        });
    }

    private void defaultNullPropertyTypes(Map<String, Property> props) {
        props.forEach((key, prop) -> {
            if (prop.getType() == null) {
                prop.setType(Property.PropertyType.STRING);
            }
        });
    }

    private Property.PropertyType resolvePropertyType(Property.PropertyType previous, Property.PropertyType current) {
        if (previous == null) return current;
        if (current == previous) return previous;
        if (current == Property.PropertyType.STRING || previous == Property.PropertyType.STRING) return Property.PropertyType.STRING;
        if (current == Property.PropertyType.DATE || previous == Property.PropertyType.DATE) return Property.PropertyType.STRING;
        if (current == Property.PropertyType.DECIMAL || previous == Property.PropertyType.DECIMAL) return Property.PropertyType.DECIMAL;
        return previous;
    }
}
