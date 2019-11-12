package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.CreateCaseParameter;
import no.fint.arkiv.p360.caze.ObjectFactory;
import no.fint.model.administrasjon.arkiv.*;
import no.fint.model.resource.Link;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.CaseDefaults;
import no.fint.p360.data.CaseProperties;
import no.fint.p360.data.utilities.Constants;
import no.fint.p360.data.utilities.P360Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Service
@Slf4j
public class TilskuddFartoyDefaults {
    @Autowired
    private CaseDefaults caseDefaults;

    private CaseProperties properties;
    private ObjectFactory objectFactory;

    @PostConstruct
    public void init() {
        properties = caseDefaults.getCasetype().get("tilskudd-fartoy");
        objectFactory = new ObjectFactory();
    }

    public void applyDefaultsForCreation(TilskuddFartoyResource tilskuddFartoy) {
        if (tilskuddFartoy.getSaksstatus().isEmpty()) {
            tilskuddFartoy.addSaksstatus(Link.with(
                    Saksstatus.class,
                    "systemid",
                    properties.getInitialCaseStatus()
            ));
        }
        if (tilskuddFartoy.getArkivdel().isEmpty()) {
            tilskuddFartoy.addArkivdel(Link.with(
                    Arkivdel.class,
                    "systemid",
                    properties.getSubArchive()
            ));
        }
        applyDefaultsForUpdate(tilskuddFartoy);
    }

    public void applyDefaultsForUpdate(TilskuddFartoyResource tilskuddFartoy) {
        tilskuddFartoy.getJournalpost().forEach(journalpost -> {
            journalpost.getKorrespondansepart().forEach(korrespondanse -> {
                if (korrespondanse.getKorrespondanseparttype().isEmpty()) {
                    korrespondanse.addKorrespondanseparttype(Link.with(
                            KorrespondansepartType.class,
                            "systemid",
                            properties.getContactRole()));
                }
            });
            journalpost.getDokumentbeskrivelse().forEach(dokumentbeskrivelse -> {
                if (dokumentbeskrivelse.getDokumentstatus().isEmpty()) {
                    dokumentbeskrivelse.addDokumentstatus(Link.with(
                            DokumentStatus.class,
                            "systemid",
                            properties.getFileStatus()
                    ));
                }
                if (dokumentbeskrivelse.getDokumentType().isEmpty()) {
                    dokumentbeskrivelse.addDokumentType(Link.with(
                            DokumentType.class,
                            "systemid",
                            properties.getFileCategory()
                    ));
                }
                if (dokumentbeskrivelse.getTilknyttetRegistreringSom().isEmpty()) {
                    dokumentbeskrivelse.addTilknyttetRegistreringSom(Link.with(
                            TilknyttetRegistreringSom.class,
                            "systemid",
                            properties.getRelationType()
                    ));
                }
            });
            if (journalpost.getJournalposttype().isEmpty()) {
                journalpost.addJournalposttype(Link.with(
                        JournalpostType.class,
                        "systemid",
                        properties.getDocumentCategory()));
            }
            if (journalpost.getJournalstatus().isEmpty()) {
                journalpost.addJournalstatus(Link.with(
                        JournalStatus.class,
                        "systemid",
                        properties.getDocumentStatus()));
            }
            if (journalpost.getJournalenhet().isEmpty()) {
                journalpost.addJournalenhet(Link.with(
                        AdministrativEnhet.class,
                        "systemid",
                        properties.getResponsibleUnit()
                ));
            }
            if (journalpost.getAdministrativEnhet().isEmpty()) {
                journalpost.addAdministrativEnhet(Link.with(
                        AdministrativEnhet.class,
                        "systemid",
                        properties.getResponsibleUnit()
                ));
            }
            if (journalpost.getArkivdel().isEmpty()) {
                journalpost.addArkivdel(Link.with(
                        Arkivdel.class,
                        "systemid",
                        properties.getSubArchive()
                ));
            }
        });
    }

    public void applyDefaultsToCreateCase(TilskuddFartoyResource tilskuddFartoy, CreateCaseParameter createCaseParameter) {
        createCaseParameter.setKeywords(P360Utils.getKeywords(Arrays.asList(properties.getKeywords())));
        createCaseParameter.setFiledOnPaper(objectFactory.createCaseParameterBaseFiledOnPaper(false));
        createCaseParameter.setCaseType(objectFactory.createCreateCaseParameterCaseType(Constants.CASE_TYPE_NOARK));
        createCaseParameter.setArchiveCodes(P360Utils.getArchiveCodes(properties.getArchiveCodeType(), tilskuddFartoy.getFartoyNavn()));
    }
}
