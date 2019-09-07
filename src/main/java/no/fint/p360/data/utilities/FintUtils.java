package no.fint.p360.data.utilities;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.contact.Address;
import no.fint.arkiv.p360.contact.ContactPersonResult;
import no.fint.arkiv.p360.contact.EnterpriseResult;
import no.fint.arkiv.p360.contact.PrivatePersonResult;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.administrasjon.arkiv.VariantformatResource;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBElement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public enum FintUtils {
    ;

    public static Identifikator createIdentifikator(String value) {
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(value);
        return identifikator;
    }

    public static boolean validIdentifikator(Identifikator input) {
        return Objects.nonNull(input) && StringUtils.isNotBlank(input.getIdentifikatorverdi());
    }

    public static Date parseDate(String value) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.ENGLISH);
        try {
            return dateFormat.parse(value);
        } catch (ParseException e) {
            log.warn("Unable to parse date {}", value);
            return null;
        }
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
        return optionalValue(result.getPostAddress()).map(FintUtils::createAdresseResource).orElse(null);

    }

    public static AdresseResource createAdresse(ContactPersonResult result) {
        return optionalValue(result.getPostAddress()).map(FintUtils::createAdresseResource).orElse(null);
    }

    private static AdresseResource createAdresseResource(Address address) {
        AdresseResource adresseResource = new AdresseResource();


        adresseResource.setAdresselinje(Collections.singletonList(address.getStreetAddress().getValue()));
        adresseResource.setPostnummer(address.getZipCode().getValue());
        adresseResource.setPoststed(address.getZipPlace().getValue());

        return adresseResource;
    }

    public static AdresseResource createAdresse(EnterpriseResult result) {
        return optionalValue(result.getPostAddress()).map(FintUtils::createAdresseResource).orElse(null);
    }

    public static Personnavn parsePersonnavn(String input) {
        Personnavn personnavn = new Personnavn();
        if (StringUtils.contains(input, ", ")) {
            personnavn.setEtternavn(StringUtils.substringBefore(input, ", "));
            personnavn.setFornavn(StringUtils.substringAfter(input, ", "));
        } else if (StringUtils.contains(input, ' ')) {
            personnavn.setEtternavn(StringUtils.substringAfterLast(input, " "));
            personnavn.setFornavn(StringUtils.substringBeforeLast(input, " "));
        } else {
            throw new IllegalArgumentException("Ugyldig personnavn: " + input);
        }
        return personnavn;
    }

    public static String getFullNameString(PrivatePersonResult result) {
        return String.format("%s %s", result.getFirstName().getValue(), result.getLastName().getValue());
    }

    public static String getFullNameString(ContactPersonResult result) {
        return String.format("%s %s", result.getFirstName().getValue(), result.getLastName().getValue());
    }

    public static String getKontaktpersonString(EnterpriseResult result) {

        if (!result.getContactRelations().getValue().getEnterpriseContactResult().isEmpty()) {
            return result.getContactRelations().getValue().getEnterpriseContactResult().get(0).getName().getValue();
        }
        return "";
    }

    public static <T> Optional<T> optionalValue(JAXBElement<T> element) {
        if (!element.isNil()) {
            return Optional.of(element.getValue());
        }
        return Optional.empty();
    }

    // FIXME: 2019-05-08 Must handle if all three elements is empty. Then we should return null
    private static Kontaktinformasjon getKontaktinformasjon(JAXBElement<String> email, JAXBElement<String> mobilePhone, JAXBElement<String> phoneNumber) {
        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        optionalValue(email).ifPresent(kontaktinformasjon::setEpostadresse);
        optionalValue(mobilePhone).ifPresent(kontaktinformasjon::setMobiltelefonnummer);
        optionalValue(phoneNumber).ifPresent(kontaktinformasjon::setTelefonnummer);
        return kontaktinformasjon;
    }


    public static VariantformatResource createVariantformat(String name) {
        VariantformatResource result = new VariantformatResource();
        result.setNavn(name);
        result.setKode(name.substring(0,1));
        result.setSystemId(createIdentifikator(name.substring(0,1)));
        return result;
    }
}
