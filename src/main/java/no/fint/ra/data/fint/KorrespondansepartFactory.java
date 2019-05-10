package no.fint.ra.data.fint;

import no.fint.arkiv.p360.contact.ContactPersonResult;
import no.fint.arkiv.p360.contact.EnterpriseResult;
import no.fint.arkiv.p360.contact.PrivatePersonResult;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartResource;
import no.fint.ra.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static no.fint.ra.data.utilities.FintUtils.createIdentifikator;
import static no.fint.ra.data.utilities.FintUtils.optionalValue;

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
        korrespondansepartResource.setSystemId(createIdentifikator(result.getRecno().toString()));
        optionalValue(result.getPersonalIdNumber())
                .filter(StringUtils::isNotBlank)
                .map(FintUtils::createIdentifikator)
                .ifPresent(korrespondansepartResource::setFodselsnummer);

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
        korrespondansepartResource.setSystemId(createIdentifikator(result.getRecno().toString()));

        return korrespondansepartResource;
    }

    public KorrespondansepartResource toFintResource(EnterpriseResult result) {

        if (result == null) {
            return null;
        }

        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();
        korrespondansepartResource.setAdresse(FintUtils.createAdresse(result));
        korrespondansepartResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        korrespondansepartResource.setKorrespondansepartNavn(result.getName().getValue());
        korrespondansepartResource.setKontaktperson(FintUtils.getKontaktpersonString(result));
        korrespondansepartResource.setSystemId(createIdentifikator(result.getRecno().toString()));
        optionalValue(result.getEnterpriseNumber())
                .filter(StringUtils::isNotBlank)
                .map(FintUtils::createIdentifikator)
                .ifPresent(korrespondansepartResource::setOrganisasjonsnummer);

        return korrespondansepartResource;
    }

}
