package no.fint.p360.data.noark.codes.klassifikasjonssystem;


import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KlassifikasjonssystemFactory {

    public KlassifikasjonssystemResource toFintResource(CodeTableRowResult codeTableRow) {
        KlassifikasjonssystemResource klassifikasjonssystemResource = new KlassifikasjonssystemResource();

        klassifikasjonssystemResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        klassifikasjonssystemResource.setTittel(codeTableRow.getCode());
        klassifikasjonssystemResource.setBeskrivelse(codeTableRow.getDescription());

        return klassifikasjonssystemResource;

    }
}
