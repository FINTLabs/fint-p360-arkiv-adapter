package no.fint.ra.data.fint;

import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.SaksstatusResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.data.noark.NoarkFactory;
import no.fint.ra.data.p360.service.P360DocumentService;
import no.fint.ra.data.utilities.FintUtils;
import no.fint.ra.data.utilities.NOARKUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class TilskuddFartoyFactory {

    @Autowired
    private P360DocumentService documentService;

    @Autowired
    private KodeverkService kodeverkService;

    @Autowired
    private NoarkFactory noarkFactory;

    public TilskuddFartoyResource toFintResource(CaseResult caseResult) {

        TilskuddFartoyResource tilskuddFartoy = new TilskuddFartoyResource();
        String caseNumber = caseResult.getCaseNumber().getValue();

        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

        // FIXME
        tilskuddFartoy.setKallesignal("AWQR");
        tilskuddFartoy.setKulturminneId("1234");
        tilskuddFartoy.setSoknadsnummer(FintUtils.createIdentifikator("1"));

        noarkFactory.getSaksmappe(caseResult, tilskuddFartoy);

        FintUtils.getSafeValue(caseResult.getStatus())
                .flatMap(kode -> kodeverkService
                        .getSaksstatus()
                        .stream()
                        .filter(it -> StringUtils.equalsIgnoreCase(kode, it.getNavn()))
                        .findAny())
                .map(SaksstatusResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(Saksstatus.class, "systemid"))
                .ifPresent(tilskuddFartoy::addSaksstatus);
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));

        return tilskuddFartoy;
    }


    public Stream<TilskuddFartoyResource> toFintResourceList(List<CaseResult> caseResult) {
        return caseResult
                .stream()
                .map(this::toFintResource);
    }
}
