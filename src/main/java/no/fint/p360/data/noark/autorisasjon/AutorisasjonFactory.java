package no.fint.p360.data.noark.autorisasjon;

import no.fint.arkiv.p360.accessgroup.AccessGroupResult;
import no.fint.arkiv.p360.accessgroup.AccessGroupUserResult;
import no.fint.arkiv.p360.accessgroup.ArrayOfAccessGroupUserResult;
import no.fint.model.administrasjon.arkiv.Arkivressurs;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.AutorisasjonResource;
import no.fint.p360.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.stream.Stream;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@Service
public class AutorisasjonFactory {
    public AutorisasjonResource toFintResource(AccessGroupResult accessGroupResult) {
        AutorisasjonResource resource = new AutorisasjonResource();


        optionalValue(accessGroupResult.getRecno())
                .map(String::valueOf)
                .filter(StringUtils::isNotBlank)
                .map(FintUtils::createIdentifikator)
                .ifPresent(resource::setSystemId);

        optionalValue(accessGroupResult.getCode())
                .map(String::valueOf)
                .filter(StringUtils::isNotBlank)
                .ifPresent(resource::setKode);

        optionalValue(accessGroupResult.getDescription())
                .map(String::valueOf)
                .filter(StringUtils::isNotBlank)
                .ifPresent(resource::setNavn);

        optionalValue(accessGroupResult.getUsers())
                .map(ArrayOfAccessGroupUserResult::getAccessGroupUserResult)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .map(AccessGroupUserResult::getADContextUser)
                .map(JAXBElement::getValue)
                .filter(StringUtils::isNotBlank)
                .map(FintUtils::identifikatorverdi)
                .map(Link.apply(Arkivressurs.class, "systemid"))
                .forEach(resource::addArkivressurs);

        return resource;
    }
}
