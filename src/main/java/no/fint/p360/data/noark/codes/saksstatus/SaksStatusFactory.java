package no.fint.p360.data.noark.codes.saksstatus;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.SaksstatusResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SaksStatusFactory {

    public SaksstatusResource toFintResource(CodeTableRowResult codeTableRow) {
        SaksstatusResource saksstatusResource = new SaksstatusResource();

        saksstatusResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        saksstatusResource.setKode(codeTableRow.getCode().getValue());
        saksstatusResource.setNavn(codeTableRow.getDescription().getValue());

        return saksstatusResource;
    }
}
