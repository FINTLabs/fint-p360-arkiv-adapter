package no.fint.ra.data.fint;

import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.*;
import no.fint.ra.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Service
public class KodeverkFactory {

    public SaksstatusResource toSaksstatus(CodeTableRowResult codeTableRow) {
        SaksstatusResource saksstatusResource = new SaksstatusResource();

        saksstatusResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        saksstatusResource.setKode(codeTableRow.getCode().getValue());
        saksstatusResource.setNavn(codeTableRow.getDescription().getValue());

        return saksstatusResource;
    }

    public DokumentStatusResource toDokumentstatus(CodeTableRowResult codeTableRow) {
        DokumentStatusResource dokumentStatusResource = new DokumentStatusResource();

        dokumentStatusResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        dokumentStatusResource.setKode(codeTableRow.getCode().getValue());
        dokumentStatusResource.setNavn(codeTableRow.getDescription().getValue());
        return dokumentStatusResource;
    }

    public JournalpostTypeResource toJournalpostType(CodeTableRowResult codeTableRow) {
        JournalpostTypeResource journalpostTypeResource = new JournalpostTypeResource();

        journalpostTypeResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        journalpostTypeResource.setKode(codeTableRow.getCode().getValue());
        journalpostTypeResource.setNavn(codeTableRow.getDescription().getValue());

        return journalpostTypeResource;
    }

    public KorrespondansepartTypeResource toKorrespondansepartType(CodeTableRowResult codeTableRow) {
        KorrespondansepartTypeResource korrespondansepartTypeResource = new KorrespondansepartTypeResource();

        korrespondansepartTypeResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        korrespondansepartTypeResource.setKode(codeTableRow.getCode().getValue());
        korrespondansepartTypeResource.setNavn(codeTableRow.getDescription().getValue());

        return korrespondansepartTypeResource;

    }

    public JournalStatusResource toJournalStatus(CodeTableRowResult codeTableRow) {
        JournalStatusResource journalStatusResource = new JournalStatusResource();

        journalStatusResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        journalStatusResource.setKode(codeTableRow.getCode().getValue());
        journalStatusResource.setNavn(codeTableRow.getDescription().getValue());

        return journalStatusResource;
    }
}
