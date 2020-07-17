package no.fint.p360.handler.noark;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.resource.FintLinks;
import no.fint.p360.data.noark.tilgang.TilgangService;
import no.fint.p360.handler.Handler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
public class GetTilgangHandler implements Handler {
    @Autowired
    private TilgangService tilgangService;

    @Override
    public void accept(Event<FintLinks> response) {
        if (StringUtils.startsWithIgnoreCase(response.getQuery(), "systemid/")) {
            tilgangService
                    .getTilgang(StringUtils.removeStartIgnoreCase(response.getQuery(), "systemid/"))
                    .ifPresent(r -> {
                        response.addData(r);
                        response.setResponseStatus(ResponseStatus.ACCEPTED);
                    });
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(ArkivActions.GET_TILGANG.name());
    }

}
