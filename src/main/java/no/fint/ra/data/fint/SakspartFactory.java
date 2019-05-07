package no.fint.ra.data.fint;

import no.fint.arkiv.p360.contact.PrivatePersonResult;
import no.fint.model.resource.administrasjon.arkiv.SakspartResource;
import no.fint.ra.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@Service
public class SakspartFactory {

    public SakspartResource toFintResourceFromPrivatePerson(PrivatePersonResult privatePersonResult) {
        SakspartResource sakspartResource = new SakspartResource();

        sakspartResource.setSakspartId(FintUtils.createIdentifikator(privatePersonResult.getRecno().toString()));
        sakspartResource.setKontaktperson(FintUtils.getFullNameString(privatePersonResult));
        sakspartResource.setAdresse(FintUtils.createAdresse(privatePersonResult));
        sakspartResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(privatePersonResult));

        return sakspartResource;
    }



}
