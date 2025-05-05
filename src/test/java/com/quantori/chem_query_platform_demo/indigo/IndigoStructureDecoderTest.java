package com.quantori.chem_query_platform_demo.indigo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.quantori.cqp.api.indigo.IndigoInchi;
import com.quantori.cqp.api.indigo.IndigoInchiProvider;
import com.quantori.cqp.api.indigo.IndigoProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class IndigoStructureDecoderTest {

    private IndigoProvider indigoProvider;
    private IndigoInchiProvider indigoInchiProvider;
    private Indigo indigo;
    private IndigoInchi inchi;
    private IndigoStructureDecoder decoder;

    @BeforeEach
    void setUp() {
        indigoProvider = mock(IndigoProvider.class);
        indigoInchiProvider = mock(IndigoInchiProvider.class);
        indigo = mock(Indigo.class);
        inchi = mock(IndigoInchi.class);

        when(indigoProvider.take()).thenReturn(indigo);
        when(indigoInchiProvider.take()).thenReturn(inchi);

        decoder = new IndigoStructureDecoder(indigoProvider, indigoInchiProvider);
    }

    @Test
    void testDeserialize_Success() {
        byte[] data = "test".getBytes();
        IndigoObject obj = mock(IndigoObject.class);

        when(indigo.deserialize(data)).thenReturn(obj);

        IndigoObject result = decoder.deserialize(data);
        assertEquals(obj, result);
        verify(indigo).deserialize(data);
        verify(indigoProvider).offer(indigo);
    }

    @Test
    void testDeserialize_ThrowsWrappedException() {
        byte[] data = "bad".getBytes();
        when(indigo.deserialize(data)).thenThrow(new RuntimeException("Fail"));

        var ex = assertThrows(MoleculeToolkitUnexpectedBehaviourException.class,
                () -> decoder.deserialize(data));
        assertTrue(ex.getMessage().contains("Deserialization with Indigo failed"));
    }

    @Test
    void testLoadMolecule_Inchi() {
        IndigoObject obj = mock(IndigoObject.class);
        when(inchi.loadMolecule(any())).thenReturn(obj);

        String inchiStr = "InChI=1S/C2H6/c1-2/h1-2H3";
        IndigoObject result = decoder.loadMolecule(inchiStr);

        assertEquals(obj, result);
        verify(inchiInchiProvider()).offer(inchi);
    }

    @Test
    void testLoadMolecule_Base64Fallback() {
        String molString = "MOCK";

        when(indigo.loadMolecule(molString)).thenThrow(new RuntimeException("fail"));
        IndigoObject obj = mock(IndigoObject.class);
        when(indigo.deserialize(any())).thenReturn(obj);

        IndigoObject result = decoder.loadMolecule(molString);
        assertEquals(obj, result);
    }

    @Test
    void testLoadMolecule_NullOrBlank() {
        assertNull(decoder.loadMolecule(null));
        assertNull(decoder.loadMolecule(""));
        assertNull(decoder.loadMolecule("  "));
    }

    @Test
    void testParseMolProperties_HandlesError() {
        Map<String, String> result = decoder.parseMolProperties("badData");
        assertTrue(result.isEmpty());
    }

    private IndigoInchiProvider inchiInchiProvider() {
        return indigoInchiProvider;
    }
}
