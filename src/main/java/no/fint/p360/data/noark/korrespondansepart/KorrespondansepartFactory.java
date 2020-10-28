package no.fint.p360.data.noark.korrespondansepart;

import no.fint.arkiv.p360.contact.*;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fint.p360.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static java.util.Optional.ofNullable;
import static no.fint.p360.data.utilities.FintUtils.*;

@SuppressWarnings("Duplicates")
@Service
public class KorrespondansepartFactory {




    @PostConstruct
    public void init() {

    }


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
        korrespondansepartResource.setKorrespondansepartNavn(result.getName());
        korrespondansepartResource.setKontaktperson(FintUtils.getKontaktpersonString(result));
        korrespondansepartResource.setSystemId(createIdentifikator(result.getRecno().toString()));
        optionalValue(result.getEnterpriseNumber())
                .filter(StringUtils::isNotBlank)
                .map(FintUtils::createIdentifikator)
                .ifPresent(korrespondansepartResource::setOrganisasjonsnummer);

        return korrespondansepartResource;
    }

    public SynchronizePrivatePersonParameter toPrivatePerson(KorrespondansepartResource korrespondansepartResource) {
        SynchronizePrivatePersonParameter privatePersonParameter = objectFactory.createSynchronizePrivatePersonParameter();
        Personnavn personnavn = parsePersonnavn(korrespondansepartResource.getKorrespondansepartNavn());
        privatePersonParameter.setFirstName(personnavn.getFornavn());
        privatePersonParameter.setLastName(personnavn.getEtternavn());
        privatePersonParameter.setPersonalIdNumber(
                korrespondansepartResource.getFodselsnummer().getIdentifikatorverdi());

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)

                .ifPresent(privatePersonParameter::setEmail);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getMobiltelefonnummer)

                .ifPresent(privatePersonParameter::setMobilePhone);

        privatePersonParameter.setPrivateAddress(createAddress(korrespondansepartResource.getAdresse()));

        return privatePersonParameter;
    }

    public SynchronizeEnterpriseParameter toEnterprise(KorrespondansepartResource korrespondansepartResource) {
        SynchronizeEnterpriseParameter parameter = objectFactory.createSynchronizeEnterpriseParameter();

        parameter.setName(korrespondansepartResource.getKorrespondansepartNavn());
        parameter.setEnterpriseNumber(korrespondansepartResource.getOrganisasjonsnummer().getIdentifikatorverdi());

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)

                .ifPresent(parameter::setEmail);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getMobiltelefonnummer)

                .ifPresent(parameter::setMobilePhone);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getTelefonnummer)

                .ifPresent(parameter::setPhoneNumber);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getNettsted)

                .ifPresent(parameter::setWeb);

        parameter.setPostAddress(createAddress(korrespondansepartResource.getAdresse()));

        return parameter;
    }

    private Address createAddress(AdresseResource adresse) {
        Address address = objectFactory.createAddress();
        address.setCountry("NOR");
        ofNullable(adresse.getAdresselinje())
                .map(l -> l.get(0))

                .ifPresent(address::setStreetAddress);
        address.setZipCode(adresse.getPostnummer());
        address.setZipPlace(adresse.getPoststed());

        return address;
    }
}
