package no.fint.ra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.adapter.event.EventResponseService;
import no.fint.adapter.event.EventStatusService;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.administrasjon.arkiv.DokumentfilResource;
import no.fint.model.resource.administrasjon.arkiv.SaksstatusResource;
import no.fint.model.resource.administrasjon.arkiv.SaksstatusResources;
import no.fint.model.resource.administrasjon.arkiv.TilknyttetRegistreringSomResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.data.FileRepository;
import no.fint.ra.data.exception.CreateTilskuddFartoyException;
import no.fint.ra.data.exception.GetTilskuddFartoyException;
import no.fint.ra.data.exception.GetTilskuddFartoyNotFoundException;
import no.fint.ra.data.p360.service.P360CaseService;
import no.fint.ra.data.p360.service.P360DocumentService;
import no.fint.ra.data.p360.service.P360FileService;
import no.fint.ra.data.p360.service.P360SupportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;


@Slf4j
@Service
public class EventHandlerService {

    @Autowired
    private EventResponseService eventResponseService;

    @Autowired
    private EventStatusService eventStatusService;

    @Autowired
    private P360DocumentService p360DocumentService;

    @Autowired
    private P360FileService p360FileService;

    @Autowired
    private P360CaseService p360CaseService;

    @Autowired
    private P360SupportService supportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileRepository fileRepository;

    public void handleEvent(String component, Event event) {
        if (event.isHealthCheck()) {
            postHealthCheckResponse(component, event);
        } else {
            if (eventStatusService.verifyEvent(component, event).getStatus() == Status.ADAPTER_ACCEPTED) {
                log.info("{}", event.getData());
                Event<FintLinks> responseEvent = new Event<>(event);
                try {

                    createEventResponse(component, event, responseEvent);

                } catch (Exception e) {
                    log.error("Error handling event {}", event, e);
                    responseEvent.setResponseStatus(ResponseStatus.ERROR);
                    responseEvent.setMessage(e.getMessage());
                }
            }
        }
    }


    private void createEventResponse(String component, Event event, Event<FintLinks> response) {

        if (KulturminnevernActions.getActions().contains(event.getAction())) {
            switch (KulturminnevernActions.valueOf(event.getAction())) {
                case UPDATE_TILSKUDDFARTOY:
                    onCreateTilskuddFartoy(component, response);
                    break;

                case GET_TILSKUDDFARTOY:
                    onGetTilskuddFartoy(component, event, response);
                    break;
            }
        }

        if (ArkivActions.getActions().contains(event.getAction())) {
            switch (ArkivActions.valueOf(event.getAction())) {
                case GET_DOKUMENTFIL:
                    onGetDokumentfil(event, response, component);
                    break;
                case GET_ALL_SAKSSTATUS:
                    onGetSaksstatus(component, response);
                    break;
                case GET_ALL_DOKUMENTSTATUS:
                    onGetAllDokumentstatus(component, response);
                    break;
                case GET_ALL_KORRESPONDANSEPARTTYPE:
                    onGetAllKorrespondansepartType(component, response);
                    break;
                case GET_ALL_TILKNYTTETREGISTRERINGSOM:
                    onGetAllTilknyttetRegistreringSom(component, response);
                    break;
            }
        }

    }

    private void onGetAllTilknyttetRegistreringSom(String component, Event<FintLinks> response) {
        supportService.getDocumentCategoryTable().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
        eventResponseService.postResponse(component, response);
    }

    private void onGetAllKorrespondansepartType(String component, Event<FintLinks> response) {
        supportService.getDocumentContactRole().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
        eventResponseService.postResponse(component, response);
    }

    private void onGetAllDokumentstatus(String component, Event<FintLinks> response) {
        supportService.getDocumentStatusTable().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
        eventResponseService.postResponse(component, response);
    }

    private void onGetSaksstatus(String component, Event<FintLinks> response) {
        supportService.getCaseStatusTable().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
        eventResponseService.postResponse(component, response);
    }

    private void onGetDokumentfil(Event event, Event<FintLinks> response, String component) {

        DokumentfilResource file = fileRepository.getFile(StringUtils.removeStartIgnoreCase(event.getQuery(), "systemid/"));
        if (file != null) {
            response.setData(Collections.singletonList(file));
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } else {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
        }

        eventResponseService.postResponse(component, response);
    }

    private void onGetTilskuddFartoy(String component, Event event, Event<FintLinks> response) {
        String query = event.getQuery();

        try {
            if (query.startsWith("mappeid")) {
                response.setData(
                        Collections.singletonList(
                                p360CaseService.getTilskuddFartoyCaseByCaseNumber(StringUtils.removeStartIgnoreCase(event.getQuery(), "systemid/"))
                        )
                );
            }
            if (query.startsWith("systemid")) {
                response.setData(
                        Collections.singletonList(
                                p360CaseService.getTilskuddFartoyCaseBySystemId(StringUtils.removeStartIgnoreCase(event.getQuery(), "systemid/"))
                        )
                );
            }
            if (query.startsWith("?")) {
                List<TilskuddFartoyResource> tilskuddFartoyResources = p360CaseService.searchTilskuddFartoyCaseByTitle(query);
                tilskuddFartoyResources.forEach(response::addData);
            }
            response.setStatus(Status.TEMP_UPSTREAM_QUEUE);
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (GetTilskuddFartoyNotFoundException e) {
            response.setMessage(e.getMessage());
            response.setData(Collections.emptyList());
        } catch (GetTilskuddFartoyException e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(String.format("Error from application: %s", e.getMessage()));
        } catch (Exception e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(String.format("Error from adapter: %s", e.getMessage()));
        }

        eventResponseService.postResponse(component, response);

    }

    private void onCreateTilskuddFartoy(String component, Event<FintLinks> response) {

        if (response.getData().size() == 1) {

            TilskuddFartoyResource tilskuddFartoyResource = objectMapper.convertValue(response.getData().get(0), TilskuddFartoyResource.class);

            try {
                TilskuddFartoyResource tilskuddFartoy = p360CaseService.createTilskuddFartoyCase(tilskuddFartoyResource);
                response.setResponseStatus(ResponseStatus.ACCEPTED);
                response.setData(Collections.singletonList(tilskuddFartoy));
            } catch (CreateTilskuddFartoyException e) {
                response.setResponseStatus(ResponseStatus.ERROR);
                response.setMessage(e.getMessage());

            }
        }
        eventResponseService.postResponse(component, response);

    }


    public void postHealthCheckResponse(String component, Event event) {
        Event<Health> healthCheckEvent = new Event<>(event);
        healthCheckEvent.setStatus(Status.TEMP_UPSTREAM_QUEUE);

        if (healthCheck()) {
            healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_HEALTHY.name()));
        } else {
            healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_UNHEALTHY));
            healthCheckEvent.setMessage("The adapter is unable to communicate with the application.");
        }

        eventResponseService.postResponse(component, healthCheckEvent);
    }


    private boolean healthCheck() {
        return p360CaseService.ping() && p360DocumentService.ping() && p360FileService.ping() && supportService.ping();

    }


    @PostConstruct
    void init() {

    }
}
