package no.fint.p360.data.noark.arkivressurs;

import no.fint.arkiv.p360.user.ArrayOfUserProfile;
import no.fint.arkiv.p360.user.UserBase;
import no.fint.arkiv.p360.user.UserProfile;
import no.fint.model.administrasjon.arkiv.Tilgang;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.ArkivressursResource;
import no.fint.p360.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@Service
public class ArkivressursFactory {

    public ArkivressursResource toFintResource(UserBase userBase) {
        ArkivressursResource arkivressurs = new ArkivressursResource();

        optionalValue(userBase.getLogin())
                .map(FintUtils::createIdentifikator)
                .ifPresent(arkivressurs::setSystemId);

        optionalValue(userBase.getContactExternalId())
                //.filter(s -> !StringUtils.startsWith(s, "recno:"))
                .map(FintUtils::createIdentifikator)
                .ifPresent(arkivressurs::setKildesystemId);

        optionalValue(userBase.getProfiles())
                .map(ArrayOfUserProfile::getUserProfile)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .map(p -> {
                    Stream.Builder<String> b = Stream.builder();
                    optionalValue(userBase.getLogin())
                            .map(FintUtils::identifikatorverdi)
                            .ifPresent(b);
                    optionalValue(p.getRole())
                            .map(FintUtils::identifikatorverdi)
                            .ifPresent(b);
                    optionalValue(p.getEnterpriseId())
                            .map(FintUtils::identifikatorverdi)
                            .ifPresent(b);
                    return b.build().collect(Collectors.joining("--"));
                })
                .map(Link.apply(Tilgang.class, "systemid"))
                .forEach(arkivressurs::addTilgang);

        return arkivressurs;
    }
}
