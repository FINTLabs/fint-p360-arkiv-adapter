package no.fint.p360.data.noark.partrolle;


import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.administrasjon.arkiv.PartRolleResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PartRolleFactory {

    public PartRolleResource toFintResource(CodeTableRowResult codeTableRow) {
        PartRolleResource partRolleResource = new PartRolleResource();

        partRolleResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        partRolleResource.setKode(codeTableRow.getCode().getValue());
        partRolleResource.setNavn(codeTableRow.getDescription().getValue());

        return partRolleResource;

    }
}
