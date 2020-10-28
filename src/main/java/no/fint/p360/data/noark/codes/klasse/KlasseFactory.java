package no.fint.p360.data.noark.codes.klasse;


import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KlasseFactory {

    public KlasseResource toFintResource(CodeTableRowResult codeTableRow) {
        KlasseResource klasseResource = new KlasseResource();

        klasseResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        klasseResource.setKlasseId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        klasseResource.setTittel(codeTableRow.getCode());
        klasseResource.setBeskrivelse(codeTableRow.getDescription());

        return klasseResource;

    }
}
