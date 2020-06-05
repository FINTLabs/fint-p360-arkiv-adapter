package no.fint.p360.handler.kulturminne;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.Problem;
import no.fint.event.model.ResponseStatus;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFredaHusPrivatEieResource;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.kulturminne.CaseDefaultsService;
import no.fint.p360.data.kulturminne.TilskuddFripFactory;
import no.fint.p360.data.p360.P360CaseService;
import no.fint.p360.data.p360.P360DocumentService;
import no.fint.p360.handler.Handler;
import no.fint.p360.service.CaseQueryService;
import no.fint.p360.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UpdateTilskuddFredaHusPrivatEieHandler implements Handler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseDefaultsService caseDefaultsService;

    @Autowired
    private TilskuddFripFactory tilskuddFripFactory;

    @Autowired
    private CaseQueryService caseQueryService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private P360DocumentService documentService;

    @Autowired
    private P360CaseService caseService;

    @Override
    public void accept(Event<FintLinks> response) {
        if (response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid request");
            return;
        }

        Operation operation = response.getOperation();

        TilskuddFredaHusPrivatEieResource tilskuddFredaHusPrivatEieResource = objectMapper.convertValue(response.getData().get(0), TilskuddFredaHusPrivatEieResource.class);

        if (operation == Operation.CREATE) {
            createCase(response, tilskuddFredaHusPrivatEieResource);
        } else if (operation == Operation.UPDATE) {
            String caseNumber = caseQueryService
                    .query(response.getQuery())
                    .map(CaseResult::getCaseNumber)
                    .filter(e -> !e.isNil())
                    .map(JAXBElement::getValue)
                    .findFirst()
                    .orElseThrow(() -> new CaseNotFound(response.getQuery()));
            updateCase(response, caseNumber, tilskuddFredaHusPrivatEieResource);
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }

    }

    private void updateCase(Event<FintLinks> response, String caseNumber, TilskuddFredaHusPrivatEieResource tilskuddFredaHusPrivatEieResource) {
        if (tilskuddFredaHusPrivatEieResource.getJournalpost() == null ||
                tilskuddFredaHusPrivatEieResource.getJournalpost().isEmpty()) {
            throw new IllegalArgumentException("Update must contain at least one Journalpost");
        }
        caseDefaultsService.applyDefaultsForUpdate("tilskudd-freda-hus-privat-eie", tilskuddFredaHusPrivatEieResource);
        log.info("Complete document for update: {}", tilskuddFredaHusPrivatEieResource);
        List<Problem> problems = validationService.getProblems(tilskuddFredaHusPrivatEieResource.getJournalpost());
        if (!problems.isEmpty()) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Payload fails validation!");
            response.setProblems(problems);
            log.info("Validation problems!\n{}\n{}\n", tilskuddFredaHusPrivatEieResource, problems);
            return;
        }
        try {
            tilskuddFredaHusPrivatEieResource.getJournalpost().stream().map(j -> tilskuddFripFactory.convertToCreateDocument(j, caseNumber)).forEach(documentService::createDocument);
            response.setData(new ArrayList<>());
            caseQueryService.query(response.getQuery()).map(tilskuddFripFactory::toFintResource).forEach(response::addData);
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (CaseNotFound | GetCaseException | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    private void createCase(Event<FintLinks> response, TilskuddFredaHusPrivatEieResource tilskuddFredaHusPrivatEieResource) {
        try {
            caseDefaultsService.applyDefaultsForCreation("tilskudd-freda-hus-privat-eie", tilskuddFredaHusPrivatEieResource);
            log.info("Complete document for creation: {}", tilskuddFredaHusPrivatEieResource);
            List<Problem> problems = validationService.getProblems(tilskuddFredaHusPrivatEieResource);
            if (!problems.isEmpty()) {
                response.setResponseStatus(ResponseStatus.REJECTED);
                response.setMessage("Payload fails validation!");
                response.setProblems(problems);
                log.info("Validation problems!\n{}\n{}\n", tilskuddFredaHusPrivatEieResource, problems);
                return;
            }
            final String caseNumber = caseService.createCase(tilskuddFripFactory.convertToCreateCase(tilskuddFredaHusPrivatEieResource));
            TilskuddFredaHusPrivatEieResource tilskuddFredaHusPrivatEie = tilskuddFripFactory.toFintResource(caseService.getCaseByCaseNumber(caseNumber));
            response.setData(ImmutableList.of(tilskuddFredaHusPrivatEie));
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (CreateCaseException | CaseNotFound | GetCaseException | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(KulturminnevernActions.UPDATE_TILSKUDDFREDAHUSPRIVATEIE.name());
    }
}
