package no.fint.ra.data.utilities;

import no.fint.arkiv.p360.contact.ContactPersonResult;
import no.fint.arkiv.p360.contact.EnterpriseResult;
import no.fint.arkiv.p360.contact.PrivatePersonResult;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;

import javax.xml.bind.JAXBElement;
import java.util.Collections;
import java.util.Optional;

public enum FintUtils {
    ;

    public static Identifikator createIdentifikator(String value) {
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(value);
        return identifikator;
    }

    public static Kontaktinformasjon createKontaktinformasjon(PrivatePersonResult result) {
        return getKontaktinformasjon(result.getEmail(), result.getMobilePhone(), result.getPhoneNumber());
    }

    public static Kontaktinformasjon createKontaktinformasjon(ContactPersonResult result) {
        return getKontaktinformasjon(result.getEmail(), result.getMobilePhone(), result.getPhoneNumber());
    }

    public static Kontaktinformasjon createKontaktinformasjon(EnterpriseResult result) {
        return getKontaktinformasjon(result.getEmail(), result.getMobilePhone(), result.getPhoneNumber());
    }


    public static AdresseResource createAdresse(PrivatePersonResult result) {
        AdresseResource adresseResource = new AdresseResource();

        adresseResource.setAdresselinje(Collections.singletonList(result.getPrivateAddress().getValue().getStreetAddress().getValue()));
        adresseResource.setPostnummer(result.getPrivateAddress().getValue().getZipCode().getValue());
        adresseResource.setPoststed(result.getPrivateAddress().getValue().getZipPlace().getValue());

        return adresseResource;
    }

    public static AdresseResource createAdresse(ContactPersonResult result) {
        AdresseResource adresseResource = new AdresseResource();
        adresseResource.setAdresselinje(Collections.singletonList(result.getPostAddress().getValue().getStreetAddress().getValue()));
        adresseResource.setPostnummer(result.getPostAddress().getValue().getZipCode().getValue());
        adresseResource.setPoststed(result.getPostAddress().getValue().getZipPlace().getValue());

        return adresseResource;
    }

    public static String getFullNameString(PrivatePersonResult result) {
        return String.format("%s %s", result.getFirstName().getValue(), result.getLastName().getValue());
    }

    public static String getFullNameString(ContactPersonResult result) {
        return String.format("%s %s", result.getFirstName().getValue(), result.getLastName());
    }

    public static String getKontaktpersonString(EnterpriseResult result) {

        if (!result.getContactRelations().getValue().getEnterpriseContactResult().isEmpty()) {
            return result.getContactRelations().getValue().getEnterpriseContactResult().get(0).getName().getValue();
        }
        return "";
    }

    public static <T> Optional<T> getSafeValue(JAXBElement<T> element) {
        if (!element.isNil()) {
            return Optional.of(element.getValue());
        }
        return Optional.empty();
    }

    private static Kontaktinformasjon getKontaktinformasjon(JAXBElement<String> email, JAXBElement<String> mobilePhone, JAXBElement<String> phoneNumber) {
        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        getSafeValue(email).ifPresent(kontaktinformasjon::setEpostadresse);
        getSafeValue(mobilePhone).ifPresent(kontaktinformasjon::setMobiltelefonnummer);
        getSafeValue(phoneNumber).ifPresent(kontaktinformasjon::setTelefonnummer);
        return kontaktinformasjon;
    }


}
