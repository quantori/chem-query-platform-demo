package com.quantori.chem_query_platform_demo.stream_task_description.properties_validator;

import com.quantori.chem_query_platform_demo.parser.SdfStructure;
import com.quantori.chem_query_platform_demo.serde.DataWrapper;
import com.quantori.chem_query_platform_demo.indigo.IndigoStructureDecoder;
import com.quantori.cqp.api.model.Property;
import com.quantori.cqp.core.task.model.DataProvider;
import com.quantori.cqp.core.task.model.StreamTaskFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class MoleculePropertyStreamTaskFunction implements StreamTaskFunction {

    private final IndigoStructureDecoder moleculeContext;

    public MoleculePropertyStreamTaskFunction(IndigoStructureDecoder moleculeContext) {
        this.moleculeContext = moleculeContext;
    }

    @Override
    public DataProvider.Data apply(DataProvider.Data data) {
        SdfStructure structure = ((DataWrapper<SdfStructure>) data).data();

        return new DataWrapper<>(extractProperties(structure));
    }

    private List<Property> extractProperties(SdfStructure structure) {
        Map<String, String> properties = moleculeContext.parseMolProperties(structure.getMoleculeString());

        if (properties.isEmpty()) {
            properties.putAll(structure.properties());
        }
        List<Property> extracted = new ArrayList<>();
        properties.forEach((name, value) -> {
            Property.PropertyType type = StringUtils.isNotEmpty(value) ? detectType(value) : null;
            extracted.add(new Property(name, type));
        });

        return extracted;
    }

    public static Property.PropertyType detectType(String value) {
        if (startsWithZeroAndIsNumber(value)) return Property.PropertyType.DECIMAL;
        if (NumberUtils.isCreatable(value)) return Property.PropertyType.DECIMAL;
        return Property.PropertyType.STRING;
    }

    private static boolean startsWithZeroAndIsNumber(String value) {
        if (value.startsWith("0") || value.startsWith("-0")) {
            String stripped = StringUtils.stripStart(value.substring(1), "0");
            return StringUtils.isBlank(stripped) || NumberUtils.isCreatable(stripped);
        }
        return false;
    }
}