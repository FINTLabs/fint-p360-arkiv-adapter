package no.fint.ra.data.fint;

import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.DokumentStatusResource;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartTypeResource;
import no.fint.model.resource.administrasjon.arkiv.SaksstatusResource;
import no.fint.model.resource.administrasjon.arkiv.TilknyttetRegistreringSomResource;
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

    public TilknyttetRegistreringSomResource toTilknyttetRegistreringSom(CodeTableRowResult codeTableRow) {
        TilknyttetRegistreringSomResource tilknyttetRegistreringSomResource = new TilknyttetRegistreringSomResource();

        tilknyttetRegistreringSomResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        tilknyttetRegistreringSomResource.setKode(codeTableRow.getCode().getValue());
        tilknyttetRegistreringSomResource.setNavn(codeTableRow.getDescription().getValue());

        return tilknyttetRegistreringSomResource;
    }

    public KorrespondansepartTypeResource toKorrespondansepartType(CodeTableRowResult codeTableRow) {
        KorrespondansepartTypeResource korrespondansepartTypeResource = new KorrespondansepartTypeResource();

        korrespondansepartTypeResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        korrespondansepartTypeResource.setKode(codeTableRow.getCode().getValue());
        korrespondansepartTypeResource.setNavn(codeTableRow.getDescription().getValue());

        return korrespondansepartTypeResource;

    }
}
