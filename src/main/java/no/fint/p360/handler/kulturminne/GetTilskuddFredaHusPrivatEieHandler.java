package no.fint.p360.handler.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.p360.data.exception.CaseNotFound;
import no.fint.p360.data.exception.GetCaseException;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.kulturminne.TilskuddFripFactory;
import no.fint.p360.handler.Handler;
import no.fint.p360.service.CaseQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
public class GetTilskuddFredaHusPrivatEieHandler implements Handler {
    @Autowired
    private TilskuddFripFactory tilskuddFripFactory;

    @Autowired
    private CaseQueryService caseQueryService;

    @Override
    public void accept(Event<FintLinks> response) {
        String query = response.getQuery();
        if (!caseQueryService.isValidQuery(query)) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid query: " + query);
            response.setStatusCode("BAD_REQUEST");
            return;
        }
        try {
            response.setData(new ArrayList<>());
            caseQueryService.query(query).map(tilskuddFripFactory::toFintResource).forEach(response::addData);
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (CaseNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        } catch (GetCaseException | GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }

    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(KulturminnevernActions.GET_TILSKUDDFREDAHUSPRIVATEIE.name());
    }

}
