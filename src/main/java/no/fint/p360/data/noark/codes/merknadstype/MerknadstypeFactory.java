package no.fint.p360.data.noark.codes.merknadstype;


import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.MerknadstypeResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MerknadstypeFactory {

    public MerknadstypeResource toFintResource(CodeTableRowResult codeTableRow) {
        MerknadstypeResource merknadstypeResource = new MerknadstypeResource();

        merknadstypeResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        merknadstypeResource.setKode(codeTableRow.getCode().getValue());
        merknadstypeResource.setNavn(codeTableRow.getDescription().getValue());

        return merknadstypeResource;

    }
}
