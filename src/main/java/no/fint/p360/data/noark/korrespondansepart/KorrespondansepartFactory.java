package no.fint.p360.data.noark.korrespondansepart;

import no.fint.arkiv.p360.contact.*;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartResource;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fint.p360.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static no.fint.p360.data.utilities.FintUtils.*;

@SuppressWarnings("Duplicates")
@Service
public class KorrespondansepartFactory {

    private ObjectFactory objectFactory;


    @PostConstruct
    public void init() {
        objectFactory = new ObjectFactory();
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
        korrespondansepartResource.setKorrespondansepartNavn(result.getName().getValue());
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
        privatePersonParameter.setFirstName(objectFactory.createPrivatePersonBaseFirstName(personnavn.getFornavn()));
        privatePersonParameter.setLastName(objectFactory.createPrivatePersonBaseLastName(personnavn.getEtternavn()));
        privatePersonParameter.setPersonalIdNumber(
                objectFactory.createPrivatePersonBasePersonalIdNumber(
                        korrespondansepartResource.getFodselsnummer().getIdentifikatorverdi()));

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)
                .map(objectFactory::createPrivatePersonBaseEmail)
                .ifPresent(privatePersonParameter::setEmail);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getMobiltelefonnummer)
                .map(objectFactory::createPrivatePersonBaseMobilePhone)
                .ifPresent(privatePersonParameter::setMobilePhone);

        privatePersonParameter.setPrivateAddress(createAddress(korrespondansepartResource.getAdresse()));

        return privatePersonParameter;
    }

    public SynchronizeEnterpriseParameter toEnterprise(KorrespondansepartResource korrespondansepartResource) {
        SynchronizeEnterpriseParameter parameter = objectFactory.createSynchronizeEnterpriseParameter();

        parameter.setName(objectFactory.createEnterpriseBaseName(
                korrespondansepartResource.getKorrespondansepartNavn()));
        parameter.setEnterpriseNumber(objectFactory.createEnterpriseBaseEnterpriseNumber(
                korrespondansepartResource.getOrganisasjonsnummer().getIdentifikatorverdi()));

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)
                .map(objectFactory::createEnterpriseBaseEmail)
                .ifPresent(parameter::setEmail);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getMobiltelefonnummer)
                .map(objectFactory::createEnterpriseBaseMobilePhone)
                .ifPresent(parameter::setMobilePhone);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getTelefonnummer)
                .map(objectFactory::createEnterpriseBasePhoneNumber)
                .ifPresent(parameter::setPhoneNumber);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getNettsted)
                .map(objectFactory::createEnterpriseBaseWeb)
                .ifPresent(parameter::setWeb);

        parameter.setPostAddress(createAddress(korrespondansepartResource.getAdresse()));

        return parameter;
    }

    private JAXBElement<Address> createAddress(AdresseResource adresse) {
        Address address = objectFactory.createAddress();
        address.setCountry(objectFactory.createAddressCountry("NOR"));
        ofNullable(adresse.getAdresselinje())
                .map(l -> l.get(0))
                .map(objectFactory::createAddressStreetAddress)
                .ifPresent(address::setStreetAddress);
        address.setZipCode(objectFactory.createAddressZipCode(adresse.getPostnummer()));
        address.setZipPlace(objectFactory.createAddressZipPlace(adresse.getPoststed()));

        return objectFactory.createAddress(address);
    }
}
