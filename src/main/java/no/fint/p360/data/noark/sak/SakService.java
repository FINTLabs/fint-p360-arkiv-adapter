package no.fint.p360.data.noark.sak;

import no.fint.model.resource.administrasjon.arkiv.SakResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.GetTilskuddFartoyException;
import no.fint.p360.data.exception.GetTilskuddFartoyNotFoundException;
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

    public List<SakResource> searchSakByTitle(Map<String, String> query) throws GetTilskuddFartoyException, GetDocumentException, IllegalCaseNumberFormat {
        return sakFactory.toFintResourceList(caseService.getGetCasesQueryByTitle(query));
    }

    public SakResource getSakByCaseNumber(String caseNumber) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException, GetDocumentException, IllegalCaseNumberFormat {
        return sakFactory.toFintResource(caseService.getSakByCaseNumber(caseNumber));
    }

    public SakResource getSakBySystemId(String systemId) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException, GetDocumentException, IllegalCaseNumberFormat {
        return sakFactory.toFintResource(caseService.getSakBySystemId(systemId));
    }

    public boolean health() {
        return caseService.ping() && sakFactory.health();
    }
}
