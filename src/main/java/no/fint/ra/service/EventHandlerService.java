package no.fint.ra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.adapter.event.EventResponseService;
import no.fint.adapter.event.EventStatusService;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.administrasjon.arkiv.DokumentfilResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.data.FileRepository;
import no.fint.ra.data.exception.*;
import no.fint.ra.data.fint.KorrespondansepartService;
import no.fint.ra.data.fint.PartService;
import no.fint.ra.data.noark.NoarkCodeListService;
import no.fint.ra.data.p360.service.*;
import no.fint.ra.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Service
public class EventHandlerService {

    @Autowired
    private EventResponseService eventResponseService;

    @Autowired
    private EventStatusService eventStatusService;

    @Autowired
    private P360DocumentService documentService;

    @Autowired
    private P360FileService fileService;

    @Autowired
    private P360CaseService caseService;

    @Autowired
    private P360SupportService supportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private NoarkCodeListService noarkCodeListService;

    @Autowired
    private KorrespondansepartService korrespondansepartService;

    @Autowired
    private PartService partService;


    @Bean
    private AtomicLong identifiers(@Value("${fint.arkiv.dokument.seq:9999999999}") Long seq) {
        return new AtomicLong(seq);
    }

    @Autowired
    private AtomicLong identifier;

    @Autowired
    private P360ContactService contactService;


    public void handleEvent(String component, Event event) {
        if (event.isHealthCheck()) {
            postHealthCheckResponse(component, event);
        } else {
            if (eventStatusService.verifyEvent(component, event)) {
                Event<FintLinks> responseEvent = new Event<>(event);
                try {
                    createEventResponse(event, responseEvent);
                    log.info("Response: {}", responseEvent);
                } catch (Exception e) {
                    log.error("Error handling event {}", event, e);
                    responseEvent.setResponseStatus(ResponseStatus.ERROR);
                    responseEvent.setMessage(ExceptionUtils.getStackTrace(e));
                } finally {
                    eventResponseService.postResponse(component, responseEvent);
                }
            }
        }
    }


    private void createEventResponse(Event event, Event<FintLinks> response) throws IOException {

        if (KulturminnevernActions.getActions().contains(event.getAction())) {
            switch (KulturminnevernActions.valueOf(event.getAction())) {
                case UPDATE_TILSKUDDFARTOY:
                    onCreateTilskuddFartoy(response);
                    break;

                case GET_TILSKUDDFARTOY:
                    onGetTilskuddFartoy(event, response);
                    break;
            }
        } else if (ArkivActions.getActions().contains(event.getAction())) {
            switch (ArkivActions.valueOf(event.getAction())) {
                case UPDATE_DOKUMENTFIL:
                    onCreateDokumentfil(response);
                    break;
                case GET_DOKUMENTFIL:
                    onGetDokumentfil(response);
                    break;
                case GET_ALL_SAKSSTATUS:
                    onGetSaksstatus(response);
                    break;
                case GET_ALL_DOKUMENTSTATUS:
                    onGetAllDokumentstatus(response);
                    break;
                case GET_ALL_KORRESPONDANSEPARTTYPE:
                    onGetAllKorrespondansepartType(response);
                    break;
                case GET_ALL_TILKNYTTETREGISTRERINGSOM:
                    onGetAllTilknyttetRegistreringSom(response);
                    break;
                case GET_ALL_JOURNALSTATUS:
                    onGetAllJournalStatus(response);
                    break;
                case GET_ALL_JOURNALPOSTTYPE:
                    onGetAllJournalpostType(response);
                    break;
                case GET_SAK:
                    onGetSak(event, response);
                    break;
                case GET_KORRESPONDANSEPART:
                    onGetKorrespondansepart(event, response);
                    break;
                case GET_PART:
                    onGetPart(event, response);
            }
        }

    }

    private void onGetPart(Event event, Event<FintLinks> response) {
        String query = event.getQuery();

        try {
            if (StringUtils.startsWithIgnoreCase(query, "partid")) {
                response.setData(
                        Collections.singletonList(
                                partService.getPartBySystemId(Integer.valueOf(StringUtils.removeStartIgnoreCase(event.getQuery(), "partid/")))
                        )
                );
            }
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (PartNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        }
    }

    private void onGetKorrespondansepart(Event event, Event<FintLinks> response) {
        String query = event.getQuery();

        try {
            if (StringUtils.startsWithIgnoreCase(query, "systemid")) {
                response.setData(
                        Collections.singletonList(
                                korrespondansepartService.getKorrespondansepartBySystemId(Integer.valueOf(StringUtils.removeStartIgnoreCase(event.getQuery(), "systemid/")))
                        )
                );
            }
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (KorrespondansepartNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        }


    }

    private void onGetSak(Event event, Event<FintLinks> response) {
        String query = event.getQuery();

        try {
            response.getData().clear();
            if (StringUtils.startsWithIgnoreCase(query, "mappeid")) {
                response.addData(caseService.getSakByCaseNumber(StringUtils.removeStartIgnoreCase(event.getQuery(), "mappeid/")));
            } else if (StringUtils.startsWithIgnoreCase(query, "systemid")) {
                response.addData(caseService.getSakBySystemId(StringUtils.removeStartIgnoreCase(event.getQuery(), "systemid/")));
            } else if (query.startsWith("?")) {
                caseService.searchSakByTitle(query).forEach(response::addData);
            } else {
                throw new IllegalArgumentException("Invalid query: " + query);
            }
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (GetTilskuddFartoyNotFoundException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        } catch (GetTilskuddFartoyException e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(String.format("Error from application: %s", e.getMessage()));
        } catch (Exception e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(String.format("Error from adapter: %s", e.getMessage()));
        }
    }

    private void onCreateDokumentfil(Event<FintLinks> response) throws IOException {
        if (response.getOperation() != Operation.CREATE || StringUtils.isNoneBlank(response.getQuery()) || response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("ILLEGAL_REQUEST");
            response.setMessage("Illegal request");
            return;
        }
        DokumentfilResource dokumentfilResource = objectMapper.convertValue(response.getData().get(0), DokumentfilResource.class);
        log.info("Format: {}, data: {}...", dokumentfilResource.getFormat(), StringUtils.substring(dokumentfilResource.getData(), 0, 25));

        // TODO
        dokumentfilResource.setSystemId(FintUtils.createIdentifikator(String.valueOf(identifier.incrementAndGet())));
        response.getData().clear();
        fileRepository.putFile(dokumentfilResource);
        response.addData(dokumentfilResource);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllJournalStatus(Event<FintLinks> response) {
        supportService.getJournalStatusTable().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllTilknyttetRegistreringSom(Event<FintLinks> response) {
        noarkCodeListService.getTilknyttetRegistreringSom().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllJournalpostType(Event<FintLinks> response) {
        supportService.getDocumentCategoryTable().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllKorrespondansepartType(Event<FintLinks> response) {
        supportService.getDocumentContactRole().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllDokumentstatus(Event<FintLinks> response) {
        supportService.getDocumentStatusTable().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetSaksstatus(Event<FintLinks> response) {
        supportService.getCaseStatusTable().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetDokumentfil(Event<FintLinks> response) {
        try {
            String query = response.getQuery();
            if (!StringUtils.startsWithIgnoreCase(query, "systemid/")) {
                response.setResponseStatus(ResponseStatus.REJECTED);
                response.setStatusCode("INVALID_QUERY");
                response.setMessage("Invalid query: " + query);
                return;
            }
            DokumentfilResource dokumentfilResource = fileRepository.getFile(StringUtils.removeStartIgnoreCase(query, "systemid/"));
            response.addData(dokumentfilResource);
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (FileNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        }
    }

    private void onGetTilskuddFartoy(Event event, Event<FintLinks> response) {
        String query = event.getQuery();

        try {
            response.getData().clear();
            if (StringUtils.startsWithIgnoreCase(query, "mappeid")) {
                response.addData(caseService.getTilskuddFartoyCaseByCaseNumber(StringUtils.removeStartIgnoreCase(event.getQuery(), "systemid/")));
            } else if (StringUtils.startsWithIgnoreCase(query, "systemid")) {
                response.addData(caseService.getTilskuddFartoyCaseBySystemId(StringUtils.removeStartIgnoreCase(event.getQuery(), "systemid/")));
            } else if (query.startsWith("?")) {
                caseService.searchTilskuddFartoyCaseByTitle(query).forEach(response::addData);
            } else {
                throw new IllegalArgumentException("Invalid query: " + query);
            }
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (GetTilskuddFartoyNotFoundException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        } catch (GetTilskuddFartoyException e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(String.format("Error from application: %s", e.getMessage()));
        } catch (Exception e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(String.format("Error from adapter: %s", e.getMessage()));
        }

    }

    private void onCreateTilskuddFartoy(Event<FintLinks> response) {

        if (response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid request");
            return;
        }

        TilskuddFartoyResource tilskuddFartoyResource = objectMapper.convertValue(response.getData().get(0), TilskuddFartoyResource.class);

        try {
            TilskuddFartoyResource tilskuddFartoy = caseService.createTilskuddFartoyCase(tilskuddFartoyResource);
            response.setData(Collections.singletonList(tilskuddFartoy));
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (CreateTilskuddFartoyException e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(e.getMessage());
        }
    }


    public void postHealthCheckResponse(String component, Event event) {
        Event<Health> healthCheckEvent = new Event<>(event);
        healthCheckEvent.setStatus(Status.TEMP_UPSTREAM_QUEUE);

        if (healthCheck()) {
            healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_HEALTHY));
            healthCheckEvent.setMessage("Connected to SIF version " + supportService.getSIFVersion());
        } else {
            healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_UNHEALTHY));
            healthCheckEvent.setMessage("The adapter is unable to communicate with the application.");
        }

        eventResponseService.postResponse(component, healthCheckEvent);
    }


    private boolean healthCheck() {
        return caseService.ping()
                && documentService.ping()
                && fileService.ping()
                && supportService.ping()
                && contactService.ping();
    }


    @PostConstruct
    void init() {

    }
}
