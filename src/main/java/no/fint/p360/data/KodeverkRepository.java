package no.fint.p360.data;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.*;
import no.fint.p360.data.noark.dokumentstatus.DokumentstatusService;
import no.fint.p360.data.noark.dokumenttype.DokumenttypeService;
import no.fint.p360.data.noark.journalposttype.JournalpostTypeService;
import no.fint.p360.data.noark.journalstatus.JournalStatusService;
import no.fint.p360.data.noark.korrespondanseparttype.KorrespondansepartTypeService;
import no.fint.p360.data.noark.merknadstype.MerknadstypeService;
import no.fint.p360.data.noark.partrolle.PartRolleService;
import no.fint.p360.data.noark.saksstatus.SaksStatusService;
import no.fint.p360.data.noark.skjermingshjemmel.SkjermingshjemmelService;
import no.fint.p360.data.noark.tilgangsrestriksjon.TilgangsrestriksjonService;
import no.fint.p360.data.noark.tilknyttetregistreringsom.TilknyttetRegistreringSomService;
import no.fint.p360.data.noark.variantformat.VariantformatService;
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
    private DokumenttypeService dokumenttypeService;

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

    @Autowired
    private TilgangsrestriksjonService tilgangsrestriksjonService;

    @Autowired
    private SkjermingshjemmelService skjermingshjemmelService;

    @Autowired
    private MerknadstypeService merknadstypeService;

    @Autowired
    private VariantformatService variantformatService;

    @Getter
    private List<SaksstatusResource> saksstatus;

    @Getter
    private List<DokumentStatusResource> dokumentStatus;

    @Getter
    private List<DokumentTypeResource> dokumentType;

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

    @Getter
    private List<TilgangsrestriksjonResource> tilgangsrestriksjon;

    @Getter
    private List<SkjermingshjemmelResource> skjermingshjemmel;

    @Getter
    private List<MerknadstypeResource> merknadstype;

    @Getter
    private List<VariantformatResource> variantformat;

    @Scheduled(initialDelay = 10000, fixedDelayString = "${fint.kodeverk.refresh-interval:1500000}")
    public void refresh() {
        saksstatus = saksStatusService.getCaseStatusTable().collect(Collectors.toList());
        dokumentStatus = dokumentstatusService.getDocumentStatusTable().collect(Collectors.toList());
        dokumentType = dokumenttypeService.getDocumenttypeTable().collect(Collectors.toList());
        journalpostType = journalpostTypeService.getDocumentCategoryTable().collect(Collectors.toList());
        korrespondansepartType = korrespondansepartTypeService.getKorrespondansepartType().collect(Collectors.toList());
        journalStatus = journalStatusService.getJournalStatusTable().collect(Collectors.toList());
        tilknyttetRegistreringSom = tilknyttetRegistreringSomService.getDocumentRelationTable().collect(Collectors.toList());
        partRolle = partRolleService.getPartRolle().collect(Collectors.toList());
        merknadstype = merknadstypeService.getMerknadstype().collect(Collectors.toList());
        tilgangsrestriksjon = tilgangsrestriksjonService.getAccessCodeTable().collect(Collectors.toList());
        skjermingshjemmel = skjermingshjemmelService.getLawTable().collect(Collectors.toList());
        variantformat = variantformatService.getVersionFormatTable().collect(Collectors.toList());
        log.info("Refreshed code lists");
    }

}
