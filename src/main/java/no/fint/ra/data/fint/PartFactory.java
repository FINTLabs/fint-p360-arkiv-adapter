package no.fint.ra.data.fint;

import no.fint.arkiv.p360.contact.ContactPersonResult;
import no.fint.arkiv.p360.contact.EnterpriseResult;
import no.fint.arkiv.p360.contact.PrivatePersonResult;
import no.fint.model.resource.administrasjon.arkiv.PartResource;
import no.fint.ra.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@SuppressWarnings("Duplicates")
@Service
public class PartFactory {

    public PartResource toFintResource(PrivatePersonResult result) {

        if (result == null) {
            return null;
        }

        PartResource partResource = new PartResource();
        partResource.setAdresse(FintUtils.createAdresse(result));
        partResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        partResource.setPartNavn(FintUtils.getFullNameString(result));
        partResource.setPartId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return partResource;
    }

    public PartResource toFintResource(ContactPersonResult result) {

        if (result == null) {
            return null;
        }

        PartResource partResource = new PartResource();
        partResource.setAdresse(FintUtils.createAdresse(result));
        partResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        partResource.setPartNavn(FintUtils.getFullNameString(result));
        partResource.setPartId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return partResource;
    }

    public PartResource toFintResource(EnterpriseResult result) {

        if (result == null) {
            return null;
        }

        PartResource partResource = new PartResource();
        partResource.setAdresse(FintUtils.createAdresse(result));
        partResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        partResource.setPartNavn(result.getName().getValue());
        partResource.setPartId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return partResource;

    }



}
