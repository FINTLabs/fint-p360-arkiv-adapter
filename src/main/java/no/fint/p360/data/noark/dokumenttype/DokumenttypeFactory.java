package no.fint.p360.data.noark.dokumenttype;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.DokumentTypeResource;
import no.fint.model.resource.administrasjon.arkiv.DokumentTypeResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DokumenttypeFactory {

    public DokumentTypeResource toFintResource(CodeTableRowResult codeTableRow) {
        DokumentTypeResource dokumentTypeResource = new DokumentTypeResource();

        dokumentTypeResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        dokumentTypeResource.setKode(codeTableRow.getCode().getValue());
        dokumentTypeResource.setNavn(codeTableRow.getDescription().getValue());
        return dokumentTypeResource;
    }
}
