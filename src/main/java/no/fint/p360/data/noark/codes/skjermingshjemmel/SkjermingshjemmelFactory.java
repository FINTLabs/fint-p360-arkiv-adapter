package no.fint.p360.data.noark.codes.skjermingshjemmel;

import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.SkjermingshjemmelResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Service
public class SkjermingshjemmelFactory {
    public SkjermingshjemmelResource toFintResource(CodeTableRowResult codeTableRow) {

        SkjermingshjemmelResource SkjermingshjemmelResource = new SkjermingshjemmelResource();

        SkjermingshjemmelResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        SkjermingshjemmelResource.setKode(codeTableRow.getCode().getValue());
        SkjermingshjemmelResource.setNavn(codeTableRow.getDescription().getValue());

        return SkjermingshjemmelResource;

    }
}
