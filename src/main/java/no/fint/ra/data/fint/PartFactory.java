package no.fint.ra.data.fint;

import no.fint.arkiv.p360.contact.PrivatePersonResult;
import no.fint.model.resource.administrasjon.arkiv.PartResource;
import no.fint.ra.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Service
public class PartFactory {

    public PartResource toFintResourceFromPrivatePerson(PrivatePersonResult privatePersonResult) {
        PartResource partResource = new PartResource();

        partResource.setPartId(FintUtils.createIdentifikator(privatePersonResult.getRecno().toString()));
        partResource.setKontaktperson(FintUtils.getFullNameString(privatePersonResult));
        partResource.setAdresse(FintUtils.createAdresse(privatePersonResult));
        partResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(privatePersonResult));

        return partResource;
    }



}
