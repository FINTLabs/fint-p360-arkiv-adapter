package no.fint.p360.data.noark.codes.tilgangsrestriksjon;

import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.TilgangsrestriksjonResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Service
public class TilgangsrestriksjonFactory {
    public TilgangsrestriksjonResource toFintResource(CodeTableRowResult codeTableRow) {

        TilgangsrestriksjonResource TilgangsrestriksjonResource = new TilgangsrestriksjonResource();

        TilgangsrestriksjonResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        TilgangsrestriksjonResource.setKode(codeTableRow.getCode().getValue());
        TilgangsrestriksjonResource.setNavn(codeTableRow.getDescription().getValue());

        return TilgangsrestriksjonResource;

    }
}
