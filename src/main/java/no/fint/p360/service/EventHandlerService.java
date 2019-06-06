package no.fint.p360.service;

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
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.data.FileRepository;
import no.fint.p360.data.KodeverkRepository;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.kulturminne.TilskuddfartoyService;
import no.fint.p360.data.noark.korrespondansepart.KorrespondansepartService;
import no.fint.p360.data.noark.part.PartService;
import no.fint.p360.data.noark.sak.SakService;
import no.fint.p360.data.p360.*;
import no.fint.p360.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import static no.fint.p360.data.utilities.QueryUtils.getQueryParams;


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
    private KorrespondansepartService korrespondansepartService;

    @Autowired
    private KodeverkRepository kodeverkRepository;

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

    @Autowired
    private TilskuddfartoyService tilskuddfartoyService;

    @Autowired
    private SakService sakService;


    public void handleEvent(String component, Event event) {
        if (event.isHealthCheck()) {
            postHealthCheckResponse(component, event);
        } else {
            if (eventStatusService.verifyEvent(component, event)) {
                Event<FintLinks> responseEvent = new Event<>(event);
                try {
                    createEventResponse(event, responseEvent);
                    log.info("Response: {}", responseEvent);
                } catch (IllegalArgumentException e) {
                    log.warn("Illegal arguments in event {}: {}", event, e.getMessage());
                    responseEvent.setResponseStatus(ResponseStatus.REJECTED);
                    responseEvent.setMessage(e.getMessage());
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
                    onUpdateTilskuddFartoy(event.getQuery(), event.getOperation(), response);
                    break;

                case GET_TILSKUDDFARTOY:
                    onGetTilskuddFartoy(event.getQuery(), response);
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
                case GET_ALL_DOKUMENTTYPE:
                    onGetAllDokumenttype(response);
                    break;
                case GET_ALL_KORRESPONDANSEPARTTYPE:
                    onGetAllKorrespondansepartType(response);
                    break;
                case GET_ALL_PARTROLLE:
                    onGetAllPartRolle(response);
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
                    onGetSak(event.getQuery(), response);
                    break;
                case GET_KORRESPONDANSEPART:
                    onGetKorrespondansepart(event.getQuery(), response);
                    break;
                case UPDATE_KORRESPONDANSEPART:
                    onUpdateKorrespondansepart(event.getQuery(), response);
                    break;
                case GET_PART:
                    onGetPart(event.getQuery(), response);
            }
        }

    }

    private void onGetPart(String query, Event<FintLinks> response) {
        try {
            if (StringUtils.startsWithIgnoreCase(query, "partid/")) {
                response.setData(
                        Collections.singletonList(
                                partService.getPartByPartId(Integer.valueOf(StringUtils.removeStartIgnoreCase(query, "partid/")))
                        )
                );
            } else {
                throw new IllegalArgumentException("Invalid query: " + query);
            }
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (PartNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        }
    }

    private void onUpdateKorrespondansepart(String query, Event<FintLinks> response) {
        if (response.getOperation() != Operation.CREATE) {
            throw new IllegalArgumentException("Illegal operation: " + response.getOperation());
        }
        if (response.getData() == null || response.getData().size() != 1) {
            throw new IllegalArgumentException("Illegal request data payload.");
        }
        KorrespondansepartResource korrespondansepartResource = objectMapper.convertValue(response.getData().get(0),
                KorrespondansepartResource.class);
        KorrespondansepartResource result = korrespondansepartService.createKorrespondansepart(korrespondansepartResource);
        response.setData(Collections.singletonList(result));
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetKorrespondansepart(String query, Event<FintLinks> response) {
        try {
            if (StringUtils.startsWithIgnoreCase(query, "systemid/")) {
                response.addData(
                        korrespondansepartService.getKorrespondansepartBySystemId(
                                Integer.valueOf(
                                        StringUtils.removeStartIgnoreCase(query, "systemid/"))));
            } else if (StringUtils.startsWith(query, "organisasjonsnummer/")) {
                response.addData(
                        korrespondansepartService.getKorrespondansepartByOrganisasjonsnummer(
                                StringUtils.removeStart(query, "organisasjonsnummer/")));
            } else if (StringUtils.startsWith(query, "fodselsnummer/")) {
                response.addData(
                        korrespondansepartService.getKorrespondansepartByFodselsnummer(
                                StringUtils.removeStart(query, "fodselsnummer/")
                        )
                );
            } else if (StringUtils.startsWith(query, "?")) {
                korrespondansepartService.search(getQueryParams(query)).forEach(response::addData);
            } else {
                throw new IllegalArgumentException("Invalid query: " + query);
            }
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (KorrespondansepartNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        }


    }

    private void onGetSak(String query, Event<FintLinks> response) {
        try {
            response.getData().clear();
            if (StringUtils.startsWithIgnoreCase(query, "mappeid/")) {
                response.addData(sakService.getSakByCaseNumber(StringUtils.removeStartIgnoreCase(query, "mappeid/")));
            } else if (StringUtils.startsWithIgnoreCase(query, "systemid/")) {
                response.addData(sakService.getSakBySystemId(StringUtils.removeStartIgnoreCase(query, "systemid/")));
            } else if (StringUtils.startsWith(query, "?")) {
                sakService.searchSakByTitle(getQueryParams(query)).forEach(response::addData);
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
            response.setMessage(String.format("Error from adapter: %s", ExceptionUtils.getStackTrace(e)));
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

        dokumentfilResource.setSystemId(FintUtils.createIdentifikator(String.format("I_%d", identifier.incrementAndGet())));
        response.getData().clear();
        fileRepository.putFile(dokumentfilResource);
        response.addData(dokumentfilResource);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllJournalStatus(Event<FintLinks> response) {
        kodeverkRepository.getJournalStatus().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllTilknyttetRegistreringSom(Event<FintLinks> response) {
        kodeverkRepository.getTilknyttetRegistreringSom().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllJournalpostType(Event<FintLinks> response) {
        kodeverkRepository.getJournalpostType().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllKorrespondansepartType(Event<FintLinks> response) {
        kodeverkRepository.getKorrespondansepartType().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllPartRolle(Event<FintLinks> response) {
        kodeverkRepository.getPartRolle().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllDokumentstatus(Event<FintLinks> response) {
        kodeverkRepository.getDokumentStatus().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllDokumenttype(Event<FintLinks> response) {
        kodeverkRepository.getDokumentType().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetSaksstatus(Event<FintLinks> response) {
        kodeverkRepository.getSaksstatus().forEach(response::addData);
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

    private void onGetTilskuddFartoy(String query, Event<FintLinks> response) {
        try {
            response.getData().clear();
            if (StringUtils.startsWithIgnoreCase(query, "mappeid/")) {
                response.addData(tilskuddfartoyService.getTilskuddFartoyCaseByCaseNumber(StringUtils.removeStartIgnoreCase(query, "mappeid/")));
            } else if (StringUtils.startsWithIgnoreCase(query, "systemid/")) {
                response.addData(tilskuddfartoyService.getTilskuddFartoyCaseBySystemId(StringUtils.removeStartIgnoreCase(query, "systemid/")));
            } else if (StringUtils.startsWithIgnoreCase(query, "soknadsnummer/")) {
                response.addData(tilskuddfartoyService.getTilskuddFartoyCaseByExternalId(StringUtils.removeStartIgnoreCase(query, "soknadsnummer/")));
            } else if (StringUtils.startsWith(query, "?")) {
                tilskuddfartoyService.searchTilskuddFartoyCaseByTitle(getQueryParams(query)).forEach(response::addData);
            } else {
                throw new IllegalArgumentException("Invalid query: " + query);
            }
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (GetTilskuddFartoyNotFoundException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        } catch (NotTilskuddfartoyException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_A_TILSKUDDFARTOY_SAK");
            response.setMessage(e.getMessage());
        } catch (GetTilskuddFartoyException e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(String.format("Error from application: %s", e.getMessage()));
        } catch (Exception e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(String.format("Error from adapter: %s", ExceptionUtils.getStackTrace(e)));
        }

    }

    private void onUpdateTilskuddFartoy(String query, Operation operation, Event<FintLinks> response) {

        if (response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid request");
            return;
        }

        TilskuddFartoyResource tilskuddFartoyResource = objectMapper.convertValue(response.getData().get(0), TilskuddFartoyResource.class);

        if (operation == Operation.CREATE) {
            try {
                TilskuddFartoyResource tilskuddFartoy = tilskuddfartoyService.createTilskuddFartoyCase(tilskuddFartoyResource);
                response.setData(Collections.singletonList(tilskuddFartoy));
                response.setResponseStatus(ResponseStatus.ACCEPTED);
            } catch (CreateCaseException e) {
                response.setResponseStatus(ResponseStatus.ERROR);
                response.setMessage(e.getMessage());
            }
        } else if (operation == Operation.UPDATE) {
            if (!StringUtils.startsWithIgnoreCase(query, "mappeid/")) {
                throw new IllegalArgumentException("Invalid query: " + query);
            }
            String caseNumber = StringUtils.removeStartIgnoreCase(query, "mappeid/");
            TilskuddFartoyResource result = tilskuddfartoyService.updateTilskuddFartoyCase(caseNumber, tilskuddFartoyResource);
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
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
}
