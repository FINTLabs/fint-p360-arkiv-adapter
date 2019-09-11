package no.fint.p360.data.noark.codes.journalstatus;


import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.JournalStatusResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JournalStatusFactory {

    public JournalStatusResource toFintResource(CodeTableRowResult codeTableRow) {
        JournalStatusResource journalStatusResource = new JournalStatusResource();

        journalStatusResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        journalStatusResource.setKode(codeTableRow.getCode().getValue());
        journalStatusResource.setNavn(codeTableRow.getDescription().getValue());

        return journalStatusResource;
    }
}
