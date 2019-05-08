package no.fint.ra.data.fint;

import no.fint.arkiv.p360.contact.ContactPersonResult;
import no.fint.arkiv.p360.contact.EnterpriseResult;
import no.fint.arkiv.p360.contact.PrivatePersonResult;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartResource;
import no.fint.ra.data.utilities.FintUtils;
import org.springframework.stereotype.Service;

@SuppressWarnings("Duplicates")
@Service
public class KorrespondansepartFactory {

    public KorrespondansepartResource toFintResource(PrivatePersonResult result) {

        if (result == null) {
            return null;
        }

        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();
        korrespondansepartResource.setAdresse(FintUtils.createAdresse(result));
        korrespondansepartResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        korrespondansepartResource.setKorrespondansepartNavn(FintUtils.getFullNameString(result));
        korrespondansepartResource.setSystemId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return korrespondansepartResource;
    }

    public KorrespondansepartResource toFintResource(ContactPersonResult result) {

        if (result == null) {
            return null;
        }

        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();
        korrespondansepartResource.setAdresse(FintUtils.createAdresse(result));
        korrespondansepartResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        korrespondansepartResource.setKorrespondansepartNavn(FintUtils.getFullNameString(result));
        korrespondansepartResource.setSystemId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return korrespondansepartResource;
    }

    public KorrespondansepartResource toFintResource(EnterpriseResult result) {

        if (result == null) {
            return null;
        }

        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();
        korrespondansepartResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        korrespondansepartResource.setKorrespondansepartNavn(result.getName().getValue());
        korrespondansepartResource.setKontaktperson(FintUtils.getKontaktpersonString(result));
        korrespondansepartResource.setSystemId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return korrespondansepartResource;
    }

}
