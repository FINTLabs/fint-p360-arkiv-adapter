package no.fint.ra.data.fint;

import no.fint.model.resource.administrasjon.arkiv.PartResource;
import no.fint.ra.data.exception.PartNotFound;
import no.fint.ra.data.p360.service.P360ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
public class PartService {

    @Autowired
    private PartFactory partFactory;

    @Autowired
    private P360ContactService contactService;

    public PartResource getPartBySystemId(int id) {

        Supplier<PartResource> enterpriseContact = () ->
                partFactory.toFintResource(contactService.getEntperiseContact(id));
        Supplier<PartResource> privateContact = () ->
                partFactory.toFintResource(contactService.getPrivatePrivateByRecno(id));
        Supplier<PartResource> contact = () ->
                partFactory.toFintResource(contactService.getContactPerson(id));

        return Stream.of(enterpriseContact, privateContact, contact)
                .parallel()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new PartNotFound("Recno " + id + " not found"));

    }
}
