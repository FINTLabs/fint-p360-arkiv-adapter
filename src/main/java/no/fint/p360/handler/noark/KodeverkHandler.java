package no.fint.p360.handler.noark;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.resource.FintLinks;
import no.fint.p360.handler.Handler;
import no.fint.p360.repository.KodeverkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static no.fint.model.administrasjon.arkiv.ArkivActions.*;

@Service
@Slf4j
public class KodeverkHandler implements Handler {

    private final EnumMap<ArkivActions, Consumer<Event<FintLinks>>> actionsMap = new EnumMap<>(ArkivActions.class);

    @PostConstruct
    public void init() {
        actionsMap.put(GET_ALL_DOKUMENTSTATUS, this::onGetAllDokumentstatus);
        actionsMap.put(GET_ALL_DOKUMENTTYPE, this::onGetAllDokumenttype);
        actionsMap.put(GET_ALL_JOURNALPOSTTYPE, this::onGetAllJournalpostType);
        actionsMap.put(GET_ALL_JOURNALSTATUS, this::onGetAllJournalStatus);
        actionsMap.put(GET_ALL_KORRESPONDANSEPARTTYPE, this::onGetAllKorrespondansepartType);
        actionsMap.put(GET_ALL_MERKNADSTYPE, this::onGetAllMerknadstype);
        actionsMap.put(GET_ALL_PARTROLLE, this::onGetAllPartRolle);
        actionsMap.put(GET_ALL_SAKSSTATUS, this::onGetSaksstatus);
        actionsMap.put(GET_ALL_SKJERMINGSHJEMMEL, this::onGetAllSkjermingshjemmel);
        actionsMap.put(GET_ALL_TILGANGSRESTRIKSJON, this::onGetAllTilgangsrestriksjon);
        actionsMap.put(GET_ALL_TILKNYTTETREGISTRERINGSOM, this::onGetAllTilknyttetRegistreringSom);
        actionsMap.put(GET_ALL_VARIANTFORMAT, this::onGetAllVariantformat);
    }

    @Autowired
    private KodeverkRepository kodeverkRepository;

    @Override
    public void accept(Event<FintLinks> response) {
        if (!health()) {
            response.setStatus(Status.ADAPTER_REJECTED);
            response.setMessage("Health test failed");
            return;
        }
        response.setResponseStatus(ResponseStatus.ACCEPTED);
        actionsMap.getOrDefault(ArkivActions.valueOf(response.getAction()),
                event -> {
                    event.setStatus(Status.ADAPTER_REJECTED);
                    event.setMessage("Unsupported action " + event.getAction());
                }).accept(response);
    }

    @Override
    public Set<String> actions() {
        return actionsMap.keySet().stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public boolean health() {
        return kodeverkRepository.health();
    }

    private void onGetAllJournalStatus(Event<FintLinks> response) {
        kodeverkRepository.getJournalStatus().forEach(response::addData);
    }

    private void onGetAllTilknyttetRegistreringSom(Event<FintLinks> response) {
        kodeverkRepository.getTilknyttetRegistreringSom().forEach(response::addData);
    }

    private void onGetAllJournalpostType(Event<FintLinks> response) {
        kodeverkRepository.getJournalpostType().forEach(response::addData);
    }

    private void onGetAllKorrespondansepartType(Event<FintLinks> response) {
        kodeverkRepository.getKorrespondansepartType().forEach(response::addData);
    }

    private void onGetAllPartRolle(Event<FintLinks> response) {
        kodeverkRepository.getPartRolle().forEach(response::addData);
    }

    private void onGetAllDokumentstatus(Event<FintLinks> response) {
        kodeverkRepository.getDokumentStatus().forEach(response::addData);
    }

    private void onGetAllDokumenttype(Event<FintLinks> response) {
        kodeverkRepository.getDokumentType().forEach(response::addData);
    }

    private void onGetSaksstatus(Event<FintLinks> response) {
        kodeverkRepository.getSaksstatus().forEach(response::addData);
    }

    private void onGetAllTilgangsrestriksjon(Event<FintLinks> response) {
        kodeverkRepository.getTilgangsrestriksjon().forEach(response::addData);
    }

    private void onGetAllSkjermingshjemmel(Event<FintLinks> response) {
        kodeverkRepository.getSkjermingshjemmel().forEach(response::addData);
    }

    private void onGetAllMerknadstype(Event<FintLinks> response) {
        kodeverkRepository.getMerknadstype().forEach(response::addData);
    }

    private void onGetAllVariantformat(Event<FintLinks> response) {
        kodeverkRepository.getVariantformat().forEach(response::addData);
    }

}
