package no.fint.ra.data.fint;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartResource;
import no.fint.ra.data.exception.ContactPersonNotFound;
import no.fint.ra.data.exception.EnterpriseNotFound;
import no.fint.ra.data.exception.KorrespondansepartNotFound;
import no.fint.ra.data.exception.PrivatePersonNotFound;
import no.fint.ra.data.p360.service.P360ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KorrespondansepartService {

    @Autowired
    private KorrespondansepartFactory korrespondansepartFactory;

    @Autowired
    private P360ContactService contactService;

    public KorrespondansepartResource getKorrespondansepartBySystemId(int id) {

        try {
            return korrespondansepartFactory.toFintResource(contactService.getContactPerson(id));
        } catch (ContactPersonNotFound e) {
            log.info(e.getMessage(), e);
        }

        try {
            return korrespondansepartFactory.toFintResource(contactService.getPrivatePrivateByRecno(id));
        } catch (PrivatePersonNotFound e) {
            log.info(e.getMessage(), e);
        }

        try {
            return korrespondansepartFactory.toFintResource(contactService.getEntperiseContact(id));
        } catch (EnterpriseNotFound e) {
            log.info(e.getMessage(), e);
        }

       throw new KorrespondansepartNotFound(String.format("%s not found", id));

    }
}
