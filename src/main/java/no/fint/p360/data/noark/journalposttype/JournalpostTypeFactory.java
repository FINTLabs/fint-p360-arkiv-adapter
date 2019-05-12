package no.fint.p360.data.noark.journalposttype;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.JournalpostTypeResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JournalpostTypeFactory {

    public JournalpostTypeResource toFintResource(CodeTableRowResult codeTableRow) {
        JournalpostTypeResource journalpostTypeResource = new JournalpostTypeResource();

        journalpostTypeResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        journalpostTypeResource.setKode(codeTableRow.getCode().getValue());
        journalpostTypeResource.setNavn(codeTableRow.getDescription().getValue());

        return journalpostTypeResource;
    }
}
