package no.fint.p360.data.noark.codes.klasse;


import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.KlasseResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KlasseFactory {

    public KlasseResource toFintResource(CodeTableRowResult codeTableRow) {
        KlasseResource klasseResource = new KlasseResource();

        klasseResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        klasseResource.setKode(codeTableRow.getCode().getValue());
        klasseResource.setNavn(codeTableRow.getDescription().getValue());

        return klasseResource;

    }
}
