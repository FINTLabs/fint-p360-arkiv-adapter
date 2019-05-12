package no.fint.p360.data.noark;

import no.fint.model.resource.administrasjon.arkiv.SakResource;
import no.fint.p360.data.fint.SakFactory;
import no.fint.p360.data.p360.P360CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.stream.Stream;

@Service
public class SakService {

    @Autowired
    private SakFactory sakFactory;

    @Autowired
    private P360CaseService caseService;

    public Stream<SakResource> searchSakByTitle(MultiValueMap<String, String> query) {
        return sakFactory.toFintResourceList(caseService.getGetCasesQueryByTitle(query));
    }

    public SakResource getSakByCaseNumber(String caseNumber) {
        return sakFactory.toFintResource(caseService.getSakByCaseNumber(caseNumber));
    }

    public SakResource getSakBySystemId(String systemId) {
        return sakFactory.toFintResource(caseService.getSakBySystemId(systemId));
    }
}
