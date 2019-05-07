package no.fint.ra.data.fint;

import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.data.noark.NoarkFactory;
import no.fint.ra.data.p360.service.P360DocumentService;
import no.fint.ra.data.utilities.FintUtils;
import no.fint.ra.data.utilities.NOARKUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TilskuddFartoyFactory {

    @Autowired
    private P360DocumentService documentService;

    @Autowired
    private NoarkFactory noarkFactory;

    public TilskuddFartoyResource toFintResource(CaseResult caseResult) {

        TilskuddFartoyResource tilskuddFartoy = new TilskuddFartoyResource();
        String caseNumber = caseResult.getCaseNumber().getValue();

        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

        tilskuddFartoy.setKallesignal("AWQR");
        tilskuddFartoy.setKulturminneId("1234");
        tilskuddFartoy.setSoknadsnummer(FintUtils.createIdentifikator("1"));


        noarkFactory.getSaksmappe(caseResult, tilskuddFartoy);

        tilskuddFartoy.addSaksstatus(Link.with(Saksstatus.class, "systemid", caseResult.getStatus().getValue()));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));

        return tilskuddFartoy;


    }


    public List<TilskuddFartoyResource> toFintResourceList(List<CaseResult> caseResult) {
        List<TilskuddFartoyResource> tilskuddFartoyList = new ArrayList<>();
        caseResult.forEach(c -> {
            TilskuddFartoyResource tilskuddFartoyResource = toFintResource(c);
            if (tilskuddFartoyResource != null) {
                tilskuddFartoyList.add(tilskuddFartoyResource);
            }
        });
        return tilskuddFartoyList;
    }
}
