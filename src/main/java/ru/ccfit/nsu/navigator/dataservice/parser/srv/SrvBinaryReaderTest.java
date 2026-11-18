package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

public class SrvBinaryReaderTest {
    @Test
    void shouldReadFileHandler() throws IOException{
        byte[] data;
        try(InputStream input = getClass().getResourceAsStream("/srv/ivm.srv")){
            assertNotNull(input, "Test srv file not found");
            data = input.readAllBytes();
        }
    }
}
