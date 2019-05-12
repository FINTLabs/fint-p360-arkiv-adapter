package no.fint.p360.data.fint;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.*;
import no.fint.p360.data.p360.P360SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KodeverkService {

    @Autowired
    private P360SupportService supportService;

    @Getter
    private List<SaksstatusResource> saksstatus;

    @Getter
    private List<DokumentStatusResource> dokumentStatus;

    @Getter
    private List<JournalpostTypeResource> journalpostType;

    @Getter
    private List<KorrespondansepartTypeResource> korrespondansepartType;

    @Getter
    private List<JournalStatusResource> journalStatus;

    @Scheduled(initialDelay = 10000, fixedDelayString = "${fint.kodeverk.refresh-interval:1500000}")
    public void refresh() {
        saksstatus = supportService.getCaseStatusTable().collect(Collectors.toList());
        dokumentStatus = supportService.getDocumentStatusTable().collect(Collectors.toList());
        journalpostType = supportService.getDocumentCategoryTable().collect(Collectors.toList());
        korrespondansepartType = supportService.getDocumentContactRole().collect(Collectors.toList());
        journalStatus = supportService.getJournalStatusTable().collect(Collectors.toList());
        log.info("Refreshed code lists");
    }

}
