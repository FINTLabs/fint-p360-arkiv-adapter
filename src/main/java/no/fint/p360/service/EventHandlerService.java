package no.fint.p360.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.adapter.event.EventResponseService;
import no.fint.adapter.event.EventStatusService;
import no.fint.event.model.*;
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
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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

    private AtomicLong identifier =
            new AtomicLong(Long
                    .parseLong(DateTimeFormatter
                            .ofPattern("yyyyDDDHH'000'")
                            .format(LocalDateTime
                                    .now())));

    @Autowired
    private P360ContactService contactService;

    @Autowired
    private TilskuddfartoyService tilskuddfartoyService;

    @Autowired
    private SakService sakService;

    @Autowired
    private ValidatorFactory validatorFactory;


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


    private void createEventResponse(Event event, Event<FintLinks> response) throws IOException, NotTilskuddfartoyException, CreateContactException, CreateEnterpriseException {

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
                case GET_ALL_TILGANGSRESTRIKSJON:
                    onGetAllTilgangsrestriksjon(response);
                    break;
                case GET_ALL_SKJERMINGSHJEMMEL:
                    onGetAllSkjermingshjemmel(response);
                    break;
                case GET_ALL_MERKNADSTYPE:
                    onGetAllMerknadstype(response);
                    break;
                case GET_ALL_VARIANTFORMAT:
                    onGetAllVariantformat(response);
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
                case UPDATE_DOKUMENTFIL:
                    onCreateDokumentfil(response);
                    break;
                case GET_DOKUMENTFIL:
                    onGetDokumentfil(response);
                    break;
            }
        }

    }

    private void onGetPart(String query, Event<FintLinks> response) {
        try {
            if (StringUtils.startsWithIgnoreCase(query, "partid/")) {
                response.setData(
                        Collections.singletonList(
                                partService.getPartByPartId(Integer.parseInt(StringUtils.removeStartIgnoreCase(query, "partid/")))
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

    private void onUpdateKorrespondansepart(String query, Event<FintLinks> response) throws CreateContactException, CreateEnterpriseException {
        if (response.getOperation() != Operation.CREATE) {
            throw new IllegalArgumentException("Illegal operation: " + response.getOperation());
        }
        if (response.getData() == null || response.getData().size() != 1) {
            throw new IllegalArgumentException("Illegal request data payload.");
        }
        KorrespondansepartResource korrespondansepartResource = objectMapper.convertValue(response.getData().get(0),
                KorrespondansepartResource.class);

        List<Problem> problems = getProblems(korrespondansepartResource);
        if (!problems.isEmpty()) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Payload fails validation!");
            response.setProblems(problems);
            return;
        }

        KorrespondansepartResource result = korrespondansepartService.createKorrespondansepart(korrespondansepartResource);
        response.setData(Collections.singletonList(result));
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetKorrespondansepart(String query, Event<FintLinks> response) {
        try {
            if (StringUtils.startsWithIgnoreCase(query, "systemid/")) {
                response.addData(
                        korrespondansepartService.getKorrespondansepartBySystemId(
                                Integer.parseInt(
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
        } catch (GetTilskuddFartoyException | GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
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

        List<Problem> problems = getProblems(dokumentfilResource);
        if (!problems.isEmpty()) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Payload fails validation!");
            response.setProblems(problems);
            return;
        }

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

    private void onGetAllTilgangsrestriksjon(Event<FintLinks> response) {
        kodeverkRepository.getTilgangsrestriksjon().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllSkjermingshjemmel(Event<FintLinks> response) {
        kodeverkRepository.getSkjermingshjemmel().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllMerknadstype(Event<FintLinks> response) {
        kodeverkRepository.getMerknadstype().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    private void onGetAllVariantformat(Event<FintLinks> response) {
        kodeverkRepository.getVariantformat().forEach(response::addData);
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
        } catch (GetTilskuddFartoyException | GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }

    }

    private void onUpdateTilskuddFartoy(String query, Operation operation, Event<FintLinks> response) throws NotTilskuddfartoyException {

        if (response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid request");
            return;
        }

        TilskuddFartoyResource tilskuddFartoyResource = objectMapper.convertValue(response.getData().get(0), TilskuddFartoyResource.class);


        if (operation == Operation.CREATE) {
            List<Problem> problems = getProblems(tilskuddFartoyResource);
            if (!problems.isEmpty()) {
                response.setResponseStatus(ResponseStatus.REJECTED);
                response.setMessage("Payload fails validation!");
                response.setProblems(problems);
                log.info("Validation problems!\n{}\n{}\n", tilskuddFartoyResource, problems);
                return;
            }
            try {
                TilskuddFartoyResource tilskuddFartoy = tilskuddfartoyService.createTilskuddFartoyCase(tilskuddFartoyResource);
                response.setData(Collections.singletonList(tilskuddFartoy));
                response.setResponseStatus(ResponseStatus.ACCEPTED);
            } catch (CreateCaseException | GetTilskuddFartoyNotFoundException | GetTilskuddFartoyException | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat e) {
                response.setResponseStatus(ResponseStatus.REJECTED);
                response.setMessage(e.getMessage());
            }
        } else if (operation == Operation.UPDATE) {
            if (!StringUtils.startsWithIgnoreCase(query, "mappeid/")) {
                throw new IllegalArgumentException("Invalid query: " + query);
            }
            if (tilskuddFartoyResource.getJournalpost().isEmpty()) {
                throw new IllegalArgumentException("Update must contain at least one Journalpost");
            }
            List<Problem> problems = getProblems(tilskuddFartoyResource.getJournalpost());
            if (!problems.isEmpty()) {
                response.setResponseStatus(ResponseStatus.REJECTED);
                response.setMessage("Payload fails validation!");
                response.setProblems(problems);
                return;
            }
            try {
                String caseNumber = StringUtils.removeStartIgnoreCase(query, "mappeid/");
                TilskuddFartoyResource result = tilskuddfartoyService.updateTilskuddFartoyCase(caseNumber, tilskuddFartoyResource);
                response.setData(Collections.singletonList(result));
                response.setResponseStatus(ResponseStatus.ACCEPTED);
            } catch (GetTilskuddFartoyNotFoundException | GetTilskuddFartoyException | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat e) {
                response.setResponseStatus(ResponseStatus.REJECTED);
                response.setMessage(e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }

    private List<Problem> getProblems(Object resource) {
        Validator validator = validatorFactory.getValidator();
        return validator.validate(resource)
                .stream()
                .map(violation -> new Problem() {{
                    setField(violation.getPropertyPath().toString());
                    setMessage(violation.getMessage());
                }})
                .collect(Collectors.toList());
    }


    public void postHealthCheckResponse(String component, Event event) {
        Event<Health> healthCheckEvent = new Event<>(event);
        healthCheckEvent.setStatus(Status.TEMP_UPSTREAM_QUEUE);

        try {
            if (healthCheck()) {
                healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_HEALTHY));
                healthCheckEvent.setMessage("Connected to SIF version " + supportService.getSIFVersion());
            } else {
                healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_UNHEALTHY));
                healthCheckEvent.setMessage("The adapter is unable to communicate with the application.");
            }
        } catch (Exception e) {
            healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_UNHEALTHY));
            healthCheckEvent.setMessage(e.getMessage());
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
