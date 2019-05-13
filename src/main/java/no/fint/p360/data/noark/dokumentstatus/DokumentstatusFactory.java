package no.fint.p360.data.noark.dokumentstatus;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.DokumentStatusResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DokumentstatusFactory {

    public DokumentStatusResource toFintResource(CodeTableRowResult codeTableRow) {
        DokumentStatusResource dokumentStatusResource = new DokumentStatusResource();

        dokumentStatusResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        dokumentStatusResource.setKode(codeTableRow.getCode().getValue());
        dokumentStatusResource.setNavn(codeTableRow.getDescription().getValue());
        return dokumentStatusResource;
    }
}
