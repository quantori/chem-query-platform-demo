package com.quantori.chem_query_platform_demo.indigo;

import com.epam.indigo.IndigoObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class IndigoMoleculeDecoder {

    private final IndigoStructureDecoder indigoStructureDecoder;

    public byte[] normalizeFromBytes(byte[] src) {

        IndigoObject molecule = decodeAndNormalize(src);
        try {
            return molecule.serialize();
        } finally {
            indigoStructureDecoder.dispose(molecule);
        }
    }

    public byte[] normalizeFromString(String molString) {
        IndigoObject molecule = null;
        try {
            molecule = indigoStructureDecoder.loadMolecule(molString);
            return molecule.serialize();
        } finally {
            indigoStructureDecoder.dispose(molecule);
        }
    }

    private IndigoObject decodeAndNormalize(byte[] src) {
        IndigoObject molecule = null;
        try {
            molecule = indigoStructureDecoder.deserialize(src);
            var valenceWarnings = molecule.checkBadValence();

            if (valenceWarnings.isEmpty()) {
                molecule.foldHydrogens();
            } else {
                log.warn("Invalid molecule structure: {}", valenceWarnings);
            }
        } catch (Exception e) {
            log.warn("Failed to decode and normalize molecule", e);
        }
        return molecule;
    }
}
