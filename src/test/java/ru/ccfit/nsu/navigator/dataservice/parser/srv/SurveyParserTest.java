
package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import org.junit.jupiter.api.Test;

import ru.ccfit.nsu.navigator.dataservice.exception.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurvey;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SurveyParserTest {

    @Test
    void shouldParseCompleteSurvey() throws Exception {
        byte[] fileData;

        try (InputStream input =
                     getClass().getResourceAsStream("/srv/ivm.srv")) {

            if (input == null) {
                throw new IllegalStateException("Test file not found");
            }

            fileData = input.readAllBytes();
        }

        SurveyParser parser = new SurveyParser();

        SrvSurvey survey = parser.parse(fileData);

        assertEquals(0x0605, survey.header().version());
        assertEquals(11924L, survey.header().stepCount());

        assertEquals(
                Set.of(0, 1, 2, 5, 6, 7),
                survey.channelData().keySet()
        );

        assertEquals(1, survey.markers().size());
        assertEquals(58, survey.rfids().size());

        assertEquals(3349, survey.vpiData().fragment().startKm());
        assertEquals(8, survey.vpiData().posts().size());
        assertEquals(2, survey.vpiData().switches().size());
        assertEquals(25, survey.vpiData().defects().size());

        assertTrue(survey.vpiData().gabarits().isEmpty());

        assertEquals(
                11902,
                survey.vpiData().geoCoordinates().size()
        );

        // Повреждённый файл: удаляем последний байт координат.
        byte[] truncated = Arrays.copyOf(
                fileData,
                fileData.length - 1
        );

        assertThrows(
                SrvParsingException.class,
                () -> parser.parse(truncated)
        );
    }
}
