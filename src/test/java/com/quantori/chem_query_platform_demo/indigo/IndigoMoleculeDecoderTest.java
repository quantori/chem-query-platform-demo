package com.quantori.chem_query_platform_demo.indigo;

import com.epam.indigo.IndigoObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IndigoMoleculeDecoderTest {

    private IndigoStructureDecoder structureDecoder;
    private IndigoMoleculeDecoder moleculeDecoder;

    @BeforeEach
    void setUp() {
        structureDecoder = mock(IndigoStructureDecoder.class);
        moleculeDecoder = new IndigoMoleculeDecoder(structureDecoder);
    }

    @Test
    void testNormalizeFromBytes_Success() {
        byte[] input = "data".getBytes();
        byte[] serialized = "normalized".getBytes();

        IndigoObject mol = mock(IndigoObject.class);

        when(structureDecoder.deserialize(input)).thenReturn(mol);
        when(mol.checkBadValence()).thenReturn("");
        when(mol.serialize()).thenReturn(serialized);

        byte[] result = moleculeDecoder.normalizeFromBytes(input);

        assertArrayEquals(serialized, result);
        verify(mol).foldHydrogens();
        verify(structureDecoder).dispose(mol);
    }

    @Test
    void testNormalizeFromBytes_WithValenceWarnings() {
        byte[] input = "valence".getBytes();
        byte[] serialized = "normalized2".getBytes();

        IndigoObject mol = mock(IndigoObject.class);

        when(structureDecoder.deserialize(input)).thenReturn(mol);
        when(mol.checkBadValence()).thenReturn("warning");
        when(mol.serialize()).thenReturn(serialized);

        byte[] result = moleculeDecoder.normalizeFromBytes(input);

        assertArrayEquals(serialized, result);
        verify(mol, never()).foldHydrogens();
        verify(structureDecoder).dispose(mol);
    }

    @Test
    void testNormalizeFromString_Success() {
        String input = "mol";
        byte[] serialized = "normalized".getBytes();

        IndigoObject mol = mock(IndigoObject.class);
        when(mol.serialize()).thenReturn(serialized);
        when(structureDecoder.loadMolecule(input)).thenReturn(mol);

        byte[] result = moleculeDecoder.normalizeFromString(input);

        assertArrayEquals(serialized, result);
        verify(structureDecoder).dispose(mol);
    }
}
