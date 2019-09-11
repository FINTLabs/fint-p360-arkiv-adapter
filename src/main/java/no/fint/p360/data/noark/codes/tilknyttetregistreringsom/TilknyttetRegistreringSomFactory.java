package no.fint.p360.data.noark.codes.tilknyttetregistreringsom;

import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.TilknyttetRegistreringSomResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Service
public class TilknyttetRegistreringSomFactory {
    public TilknyttetRegistreringSomResource toFintResource(CodeTableRowResult codeTableRow) {

        TilknyttetRegistreringSomResource TilknyttetRegistreringSomResource = new TilknyttetRegistreringSomResource();

        TilknyttetRegistreringSomResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        TilknyttetRegistreringSomResource.setKode(codeTableRow.getCode().getValue());
        TilknyttetRegistreringSomResource.setNavn(codeTableRow.getDescription().getValue());

        return TilknyttetRegistreringSomResource;

    }
}
