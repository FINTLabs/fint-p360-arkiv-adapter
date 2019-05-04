package no.fint.ra.data.fint;

import no.fint.arkiv.p360.caze.CaseDocumentResult;
import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.data.utilities.FintUtils;
import no.fint.ra.data.utilities.NOARKUtils;
import no.fint.ra.data.p360.service.P360DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TilskuddFartoyFactory {

    @Autowired
    private P360DocumentService documentService;

    public TilskuddFartoyResource toFint(CaseResult caseResult) {

        TilskuddFartoyResource tilskuddFartoy = new TilskuddFartoyResource();

        String caseNumber = caseResult.getCaseNumber().getValue();

        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

        tilskuddFartoy.setMappeId(FintUtils.createIdentifikator(caseNumber));
        tilskuddFartoy.setSystemId(FintUtils.createIdentifikator(caseResult.getRecno().toString()));
        tilskuddFartoy.setSakssekvensnummer(sequenceNumber);
        tilskuddFartoy.setSaksaar(caseYear);
        tilskuddFartoy.setSaksdato(caseResult.getDate().toGregorianCalendar().getTime());
        tilskuddFartoy.setOpprettetDato(caseResult.getCreatedDate().getValue().toGregorianCalendar().getTime());
        tilskuddFartoy.setTittel(caseResult.getUnofficialTitle().getValue());
        tilskuddFartoy.setOffentligTittel(caseResult.getTitle().getValue());
        tilskuddFartoy.setNoekkelord(Collections.emptyList());
        tilskuddFartoy.setKallesignal("AWQR");
        tilskuddFartoy.setKulturminneId("1234");
        tilskuddFartoy.setSoknadsnummer(FintUtils.createIdentifikator("1"));
        tilskuddFartoy.setBeskrivelse(caseResult.getNotes().getValue());
        tilskuddFartoy.setArkivnotat(Collections.emptyList());


        List<JournalpostResource> journalpostResourceList = new ArrayList<>();
        List<CaseDocumentResult> caseDocumentResult = caseResult.getDocuments().getValue().getCaseDocumentResult();
        caseDocumentResult.forEach(doc ->
                journalpostResourceList.add(documentService.getJournalPost(doc.getRecno().toString()))
        );
        tilskuddFartoy.setJournalpost(journalpostResourceList);

        tilskuddFartoy.addSaksstatus(Link.with(Saksstatus.class, "systemid", caseResult.getStatus().getValue()));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));

        return tilskuddFartoy;


    }



    public List<TilskuddFartoyResource> p360ToFintTilskuddFartoys(List<CaseResult> caseResult) {
        List<TilskuddFartoyResource> tilskuddFartoyList = new ArrayList<>();
        caseResult.forEach(c -> {
            TilskuddFartoyResource tilskuddFartoyResource = toFint(c);
            if (tilskuddFartoyResource != null) {
                tilskuddFartoyList.add(tilskuddFartoyResource);
            }
        });
        return tilskuddFartoyList;
    }
}
