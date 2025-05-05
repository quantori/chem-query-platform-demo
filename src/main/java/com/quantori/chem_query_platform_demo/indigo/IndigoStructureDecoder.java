package com.quantori.chem_query_platform_demo.indigo;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.quantori.cqp.api.indigo.IndigoInchi;
import com.quantori.cqp.api.indigo.IndigoInchiProvider;
import com.quantori.cqp.api.indigo.IndigoProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class IndigoStructureDecoder {

    private static final String INCHI_PREFIX = "InChI=";

    private final IndigoProvider indigoProvider;
    private final IndigoInchiProvider indigoInchiProvider;

    public Map<String, String> parseMolProperties(String str) {
        Map<String, String> propertiesMap = new LinkedHashMap<>();
        try {
            IndigoObject molecule = loadMolecule(str);
            try {
                molecule.iterateProperties().forEach(property -> {
                    String name = property.name();
                    String value = property.rawData();
                    propertiesMap.put(name, value);
                    property.dispose();
                });
            } finally {
                dispose(molecule);
            }
        } catch (Exception e) {
            log.error("Failed to parse molecule properties", e);
        }
        return propertiesMap;
    }

    IndigoObject deserialize(byte[] src) {
        Indigo indigo = indigoProvider.take();
        try {
            return deserializeWithIndigo(indigo, src);
        } finally {
            indigoProvider.offer(indigo);
        }
    }

    private IndigoObject deserializeWithIndigo(Indigo indigo, byte[] src) {
        try {
            return indigo.deserialize(src);
        } catch (Exception e) {
            throw new MoleculeToolkitUnexpectedBehaviourException("Deserialization with Indigo failed", e);
        }
    }

    IndigoObject loadMolecule(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }

        return str.startsWith(INCHI_PREFIX)
                ? loadInchiMolecule(str)
                : loadCommonMolecule(str);
    }

    private IndigoObject loadCommonMolecule(String str) {
        Indigo indigo = indigoProvider.take();
        try {
            return tryLoadMolecule(indigo, str);
        } finally {
            indigoProvider.offer(indigo);
        }
    }

    private IndigoObject tryLoadMolecule(Indigo indigo, String str) {
        try {
            return indigo.loadMolecule(str);
        } catch (Exception e) {
            log.warn("Failed to load molecule from string, attempting Base64 decode", e);
            return deserializeWithIndigo(indigo, Base64.getDecoder().decode(str));
        }
    }

    private IndigoObject loadInchiMolecule(String str) {
        IndigoInchi inchi = indigoInchiProvider.take();
        try {
            return tryLoadInchi(inchi, str);
        } finally {
            indigoInchiProvider.offer(inchi);
        }
    }

    private IndigoObject tryLoadInchi(IndigoInchi inchi, String str) {
        try {
            return inchi.loadMolecule(str);
        } catch (Exception e) {
            log.warn("Failed to load InChI molecule from string, attempting Base64 decode", e);
            return deserialize(Base64.getDecoder().decode(str));
        }
    }

    void dispose(IndigoObject indigoObject) {
        if (indigoObject != null) {
            try {
                indigoObject.dispose();
            } catch (Exception e) {
                log.warn("Failed to dispose IndigoObject", e);
            }
        }
    }
}
