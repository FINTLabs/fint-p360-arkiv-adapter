package no.fint.p360.data.noark.sak;


import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.model.arkiv.kodeverk.Saksstatus;
import no.fint.model.arkiv.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.utilities.NOARKUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SakFactory {

    @Autowired
    private NoarkFactory noarkFactory;

    public SakResource toFintResource(CaseResult caseResult) throws GetDocumentException, IllegalCaseNumberFormat {

        SakResource sakResource = new SakResource();
        String caseNumber = caseResult.getCaseNumber();

        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

        noarkFactory.getSaksmappe(caseResult, sakResource);

        sakResource.addSaksstatus(Link.with(Saksstatus.class, "systemid", caseResult.getStatus()));
        sakResource.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        sakResource.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));

        return sakResource;
    }

    public List<SakResource> toFintResourceList(List<CaseResult> caseResults) throws GetDocumentException, IllegalCaseNumberFormat {
        List<SakResource> result = new ArrayList<>(caseResults.size());
        for (CaseResult caseResult : caseResults) {
            result.add(toFintResource(caseResult));
        }
        return result;
    }

    public boolean health() {
        return noarkFactory.health();
    }
}
