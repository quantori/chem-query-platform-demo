package com.quantori.chem_query_platform_demo.stream_task_description.molecule_uploader;

import static com.quantori.chem_query_platform_demo.parser.SDFParser.EMPTY_STRUCTURE;

import com.quantori.chem_query_platform_demo.parser.SdfStructure;
import com.quantori.chem_query_platform_demo.serde.DataWrapper;
import com.quantori.chem_query_platform_demo.indigo.IndigoStructureDecoder;
import com.quantori.chem_query_platform_demo.indigo.IndigoMoleculeDecoder;
import com.quantori.cqp.api.model.upload.Molecule;
import com.quantori.cqp.core.task.model.DataProvider;
import com.quantori.cqp.core.task.model.StreamTaskFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class MoleculeLoaderStreamTaskFunction implements StreamTaskFunction {
    private static final String DEFAULT_MOLECULE = "SU0yA1zgB85mQTMz58AAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    private final IndigoStructureDecoder indigoStructureDecoder;
    private final IndigoMoleculeDecoder indigoMoleculeDecoder;
    private long order = 1L;
    @Override
    public DataProvider.Data apply(DataProvider.Data data) {
        List<SdfStructure> sdfStructures =
                ((DataWrapper<List<SdfStructure>>) data).data();
        return new DataWrapper<>(getTransform(sdfStructures));
    }

    private List<ParsingRecord> getTransform(List<SdfStructure> fileStructures) {
        Set<ImmutablePair<String, String>> renameMap = new HashSet<>();

        return fileStructures.stream().map(parsedStructure -> {
            if (isBadMolecule(parsedStructure)) {
                return getEmptyParsingRecordPair(renameMap, parsedStructure,
                        parsedStructure.error());
            }
            try {
                return getParsingRecordPair(renameMap, parsedStructure);
            } catch (Exception e) {
                return getEmptyParsingRecordPair(renameMap, parsedStructure, e.getMessage());
            }
        }).toList();
    }

    private boolean isBadMolecule(SdfStructure fileStructure) {
        return fileStructure.error() != null;
    }

    private ParsingRecord getParsingRecordPair(Set<ImmutablePair<String, String>> renameMap,
                                                           SdfStructure fileStructure) {
        String moleculeFromFile = fileStructure.getMoleculeString();

        Map<String, String> properties = indigoStructureDecoder.parseMolProperties(moleculeFromFile);

        String mol = fileStructure.error() == null ? moleculeFromFile : DEFAULT_MOLECULE;
        byte[] structure = indigoMoleculeDecoder.normalizeFromString(mol);

        renameMap.forEach(p -> {
            properties.put(p.getRight(), properties.get(p.getLeft()));
            properties.remove(p.getLeft());
        });

        Molecule molecule = new Molecule();
        molecule.setStructure(structure);
        molecule.setMolProperties(properties);
        molecule.setCustomOrder(order++);

        return new ParsingRecord(molecule, fileStructure.error());
    }

    private ParsingRecord getEmptyParsingRecordPair(Set<ImmutablePair<String, String>> renameMap,
                                                                SdfStructure fileStructure,
                                                                String error) {
        var emptyMolecule = new SdfStructure(EMPTY_STRUCTURE, fileStructure.properties(), error);
        try {
            return getParsingRecordPair(renameMap, emptyMolecule);
        } catch (Exception e) {
            return getParsingRecordPair(renameMap, new SdfStructure(EMPTY_STRUCTURE, Map.of(), error));
        }
    }
}
