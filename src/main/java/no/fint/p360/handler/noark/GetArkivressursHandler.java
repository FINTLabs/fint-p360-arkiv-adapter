package no.fint.p360.handler.noark;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.resource.FintLinks;
import no.fint.p360.data.noark.arkivressurs.ArkivressursService;
import no.fint.p360.handler.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
public class GetArkivressursHandler implements Handler {
    @Autowired
    private ArkivressursService arkivressursService;

    @Override
    public void accept(Event<FintLinks> response) {
        arkivressursService.getArkivressurs().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(ArkivActions.GET_ALL_ARKIVRESSURS.name());
    }

    @Override
    public boolean health() {
        return arkivressursService.health();
    }
}
