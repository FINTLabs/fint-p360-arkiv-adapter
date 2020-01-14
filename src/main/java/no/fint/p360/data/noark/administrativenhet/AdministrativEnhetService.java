package no.fint.p360.data.noark.administrativenhet;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.AdministrativEnhetResource;
import no.fint.p360.data.p360.P360ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@Slf4j
public class AdministrativEnhetService {

    @Autowired
    private AdministrativEnhetFactory administrativEnhetFactory;

    @Autowired
    private P360ContactService contactService;

    public Stream<AdministrativEnhetResource> getAdministrativEnhet() {
        return contactService
                .getEnterprisesByCategories("recno:1")
                .getEnterpriseResult()
                .stream()
                .map(administrativEnhetFactory::toFintResource);
    }

    public boolean health() {
        return contactService.ping();
    }
}
