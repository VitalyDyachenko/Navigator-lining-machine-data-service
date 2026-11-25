package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.*;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.ChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.SurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvVpiHeader;

import java.time.LocalDateTime;

public class SrvHeaderParser {

    public SrvHeader parse(BinaryReader reader) {
        String signature = reader.readAsciiString(6);
        if (!signature.equals("NavSRV")) {
            throw new SrvParsingException(
                    "Invalid SRV file signature: " + signature
            );
        }

        int version = reader.readUnsignedShort();

        LocalDateTime surveyTime = new TimeParser().parse(reader);
        MachineData machine = new MachineParser().parse(reader);
        SurveyFlags flags = new SurveyFlagsParser().parse(reader);

        int channelMask = reader.readUnsignedShort();
        int stepSizeMm = reader.readUnsignedShort();
        long stepCount = reader.readUnsignedInt();
        int markerCount = reader.readUnsignedShort();
        int rfidCount = reader.readUnsignedShort();

        TrackData track = new TrackParser().parse(reader);
        SrvVpiHeader vpi = new SrvVpiHeaderParser().parse(reader);
        ChannelInfo[] channels = new ChannelInfoParser().parse(reader);

        // Выравнивание заголовка
        byte[] undocumentedHeaderBytes = reader.readBytes(105);

        return new SrvHeader(
                signature,
                version,
                surveyTime,
                machine,
                flags,
                channelMask,
                stepSizeMm,
                stepCount,
                markerCount,
                rfidCount,
                track,
                vpi,
                channels,
                undocumentedHeaderBytes
        );
    }
}