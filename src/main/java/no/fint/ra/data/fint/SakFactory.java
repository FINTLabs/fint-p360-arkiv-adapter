package no.fint.ra.data.fint;


import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.SakResource;
import no.fint.ra.data.noark.NoarkFactory;
import no.fint.ra.data.utilities.NOARKUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class SakFactory {

    @Autowired
    private NoarkFactory noarkFactory;

    public SakResource toFintResource(CaseResult caseResult) {

        SakResource sakResource = new SakResource();
        String caseNumber = caseResult.getCaseNumber().getValue();

        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);


        noarkFactory.getSaksmappe(caseResult, sakResource);

        sakResource.addSaksstatus(Link.with(Saksstatus.class, "systemid", caseResult.getStatus().getValue()));
        sakResource.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        sakResource.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));

        return sakResource;
    }

    public Stream<SakResource> toFintResourceList(List<CaseResult> caseResult) {
        return caseResult
                .stream()
                .map(this::toFintResource);
    }
}
