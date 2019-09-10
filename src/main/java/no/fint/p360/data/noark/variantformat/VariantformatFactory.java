package no.fint.p360.data.noark.variantformat;

import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.VariantformatResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Service
public class VariantformatFactory {
    public VariantformatResource toFintResource(CodeTableRowResult codeTableRow) {

        VariantformatResource VariantformatResource = new VariantformatResource();

        VariantformatResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        VariantformatResource.setKode(codeTableRow.getCode().getValue());
        VariantformatResource.setNavn(codeTableRow.getDescription().getValue());

        return VariantformatResource;

    }
}
