package ru.ccfit.nsu.navigator.dataservice.parser.crm;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.CrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmCorrection;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmFragment;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmFragmentData;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmFragmentHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmHeader;

import java.util.ArrayList;
import java.util.List;

public class CorrectionParser {

	// Полный разбор двоичного файла истории выправки
	public CrmCorrection parse(byte[] fileData) {
		BinaryReader reader = new BinaryReader(fileData, CrmParsingException::new);

		CrmHeader header = new CrmHeaderParser().parse(reader);
		List<CrmFragmentHeader> fragmentHeaders = new CrmFragmentHeaderParser().parseAll(reader, header);

		CrmFragmentDataParser dataParser = new CrmFragmentDataParser();
		List<CrmFragment> fragments = new ArrayList<>(fragmentHeaders.size());

		for (CrmFragmentHeader fragmentHeader : fragmentHeaders) {
			CrmFragmentData fragmentData = dataParser.parse(reader, header, fragmentHeader);
			fragments.add(new CrmFragment(fragmentHeader, fragmentData));
		}

		if (reader.remaining() != 0) {
			throw new CrmParsingException(
					"Unexpected trailing data at offset " + reader.position()
			);
		}

		return new CrmCorrection(header, fragments);
	}
}