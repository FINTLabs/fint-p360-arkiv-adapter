package no.fint.p360.data;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.*;
import no.fint.p360.data.noark.dokumentstatus.DokumentstatusService;
import no.fint.p360.data.noark.journalposttype.JournalpostTypeService;
import no.fint.p360.data.noark.journalstatus.JournalStatusService;
import no.fint.p360.data.noark.korrespondanseparttype.KorrespondansepartTypeService;
import no.fint.p360.data.noark.partrolle.PartRolleService;
import no.fint.p360.data.noark.saksstatus.SaksStatusService;
import no.fint.p360.data.noark.tilknyttetregistreringsom.TilknyttetRegistreringSomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KodeverkRepository {

    @Autowired
    private SaksStatusService saksStatusService;

    @Autowired
    private DokumentstatusService dokumentstatusService;

    @Autowired
    private JournalpostTypeService journalpostTypeService;

    @Autowired
    private KorrespondansepartTypeService korrespondansepartTypeService;

    @Autowired
    private PartRolleService partRolleService;

    @Autowired
    private JournalStatusService journalStatusService;

    @Autowired
    private TilknyttetRegistreringSomService tilknyttetRegistreringSomService;

    @Getter
    private List<SaksstatusResource> saksstatus;

    @Getter
    private List<DokumentStatusResource> dokumentStatus;

    @Getter
    private List<JournalpostTypeResource> journalpostType;

    @Getter
    private List<KorrespondansepartTypeResource> korrespondansepartType;

    @Getter
    private List<PartRolleResource> partRolle;

    @Getter
    private List<TilknyttetRegistreringSomResource> tilknyttetRegistreringSom;

    @Getter
    private List<JournalStatusResource> journalStatus;

    @Scheduled(initialDelay = 10000, fixedDelayString = "${fint.kodeverk.refresh-interval:1500000}")
    public void refresh() {
        saksstatus = saksStatusService.getCaseStatusTable().collect(Collectors.toList());
        dokumentStatus = dokumentstatusService.getDocumentStatusTable().collect(Collectors.toList());
        journalpostType = journalpostTypeService.getDocumentCategoryTable().collect(Collectors.toList());
        korrespondansepartType = korrespondansepartTypeService.getKorrespondansepartType().collect(Collectors.toList());
        journalStatus = journalStatusService.getJournalStatusTable().collect(Collectors.toList());
        tilknyttetRegistreringSom = tilknyttetRegistreringSomService.getTilknyttetRegistreringSom();
        partRolle = partRolleService.getPartRolle().collect(Collectors.toList());
        log.info("Refreshed code lists");
    }

}
