package no.fint.ra.data.fint;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartResource;
import no.fint.ra.data.exception.KorrespondansepartNotFound;
import no.fint.ra.data.p360.service.P360ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@Service
public class KorrespondansepartService {

    @Autowired
    private KorrespondansepartFactory korrespondansepartFactory;

    @Autowired
    private P360ContactService contactService;

    public KorrespondansepartResource getKorrespondansepartBySystemId(int id) {

        Supplier<KorrespondansepartResource> enterpriseContact = () ->
                korrespondansepartFactory.toFintResource(contactService.getEnterpriseByRecno(id));
        Supplier<KorrespondansepartResource> privateContact = () ->
                korrespondansepartFactory.toFintResource(contactService.getPrivatePersonByRecno(id));
        Supplier<KorrespondansepartResource> contact = () ->
                korrespondansepartFactory.toFintResource(contactService.getContactPersonByRecno(id));

        return Stream.of(enterpriseContact, privateContact, contact)
                .parallel()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new KorrespondansepartNotFound("Recno " + id + " not found"));

    }
}
