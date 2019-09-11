package no.fint.p360.data.noark.codes.korrespondanseparttype;


import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartTypeResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KorrespondansepartTypeFactory {

    public KorrespondansepartTypeResource toFintResource(CodeTableRowResult codeTableRow) {
        KorrespondansepartTypeResource korrespondansepartTypeResource = new KorrespondansepartTypeResource();

        korrespondansepartTypeResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        korrespondansepartTypeResource.setKode(codeTableRow.getCode().getValue());
        korrespondansepartTypeResource.setNavn(codeTableRow.getDescription().getValue());

        return korrespondansepartTypeResource;

    }
}
