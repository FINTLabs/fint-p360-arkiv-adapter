package no.fint.p360.data.noark.administrativenhet;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.contact.EnterpriseResult;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.AdministrativEnhetResource;
import no.fint.p360.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@Service
@Slf4j
public class AdministrativEnhetFactory {

    public AdministrativEnhetResource toFintResource(EnterpriseResult enterpriseResult) {
        AdministrativEnhetResource resource = new AdministrativEnhetResource();

        resource.setSystemId(FintUtils.createIdentifikator(String.valueOf(enterpriseResult.getRecno())));

        optionalValue(enterpriseResult.getName())
                .ifPresent(resource::setNavn);

        optionalValue(enterpriseResult.getEnterpriseNumber())
                .filter(StringUtils::isNotBlank)
                .map(Link.apply(Organisasjonselement.class, "organisasjonsnummer"))
                .ifPresent(resource::addOrganisasjonselement);

        return resource;
    }
}
