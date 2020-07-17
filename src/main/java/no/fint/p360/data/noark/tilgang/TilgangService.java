package no.fint.p360.data.noark.tilgang;

import no.fint.model.administrasjon.arkiv.AdministrativEnhet;
import no.fint.model.administrasjon.arkiv.Arkivressurs;
import no.fint.model.administrasjon.arkiv.Rolle;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.TilgangResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class TilgangService {

    public Optional<TilgangResource> getTilgang(String systemId) {
        if (StringUtils.isBlank(systemId)) {
            return Optional.empty();
        }
        String[] strings = systemId.split("--", 3);
        if (strings.length != 3) {
            return Optional.empty();
        }

        TilgangResource resource = new TilgangResource();

        if (containsValue(strings[0], Link.apply(Arkivressurs.class, "systemid"), resource::addArkivressurs) ||
                containsValue(strings[1], Link.apply(Rolle.class, "systemid"), resource::addRolle) ||
                containsValue(strings[2], Link.apply(AdministrativEnhet.class, "systemid"), resource::addAdministrativEnhet)) {
            return Optional.of(resource);
        }

        return Optional.empty();
    }

    private boolean containsValue(String s, Function<String, Link> function, Consumer<Link> consumer) {
        if (StringUtils.isNotBlank(s)) {
            consumer.accept(function.apply(s));
            return true;
        }
        return false;
    }
}
