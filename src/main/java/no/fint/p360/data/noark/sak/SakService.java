package no.fint.p360.data.noark.sak;

import no.fint.model.resource.administrasjon.arkiv.SakResource;
import no.fint.p360.data.exception.CaseNotFound;
import no.fint.p360.data.exception.GetCaseException;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.p360.P360CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SakService {

    @Autowired
    private SakFactory sakFactory;

    @Autowired
    private P360CaseService caseService;

    public List<SakResource> searchSakByTitle(Map<String, String> query) throws GetCaseException, GetDocumentException, IllegalCaseNumberFormat {
        return sakFactory.toFintResourceList(caseService.getGetCasesQueryByTitle(query));
    }

    public SakResource getSakByCaseNumber(String caseNumber) throws CaseNotFound, GetCaseException, GetDocumentException, IllegalCaseNumberFormat {
        return sakFactory.toFintResource(caseService.getCaseByCaseNumber(caseNumber));
    }

    public SakResource getSakBySystemId(String systemId) throws CaseNotFound, GetCaseException, GetDocumentException, IllegalCaseNumberFormat {
        return sakFactory.toFintResource(caseService.getCaseBySystemId(systemId));
    }

    public boolean health() {
        return caseService.ping() && sakFactory.health();
    }
}
