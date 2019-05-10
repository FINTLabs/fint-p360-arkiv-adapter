package no.fint.ra.data.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.*;
import no.fint.model.resource.administrasjon.arkiv.SakResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.data.exception.CreateTilskuddFartoyException;
import no.fint.ra.data.exception.GetTilskuddFartoyException;
import no.fint.ra.data.exception.GetTilskuddFartoyNotFoundException;
import no.fint.ra.data.fint.SakFactory;
import no.fint.ra.data.fint.TilskuddFartoyFactory;
import no.fint.ra.data.p360.P360CaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class P360CaseService extends P360AbstractService {

    @Autowired
    private P360CaseFactory caseFactory;

    @Autowired
    private TilskuddFartoyFactory tilskuddFartoyFactory;

    @Autowired
    private SakFactory sakFactory;

    @Autowired
    private P360DocumentService documentService;

    private ICaseService caseServicePort;

    private ObjectFactory objectFactory;


    public P360CaseService() {
        super("http://software-innovation.com/SI.Data", "CaseService");
    }

    @PostConstruct
    public void init() {
        caseServicePort = new CaseService(CaseService.WSDL_LOCATION, serviceName).getBasicHttpBindingICaseService();
        super.addAuthentication(caseServicePort);

        objectFactory = new ObjectFactory();
    }

    public boolean ping() {
        try {
            caseServicePort.ping();
        } catch (WebServiceException e) {
            return false;
        }

        return true;
    }

    public TilskuddFartoyResource createTilskuddFartoyCase(TilskuddFartoyResource tilskuddFartoy) {
        CaseOperationResult caseOperationResult = caseServicePort.createCase(caseFactory.createTilskuddFartoy(tilskuddFartoy));

        if (caseOperationResult.isSuccessful()) {
            TilskuddFartoyResource tilskuddFartoyNew = getTilskuddFartoyCaseByCaseNumber(caseOperationResult.getCaseNumber().getValue());
            tilskuddFartoyNew.setJournalpost(tilskuddFartoy.getJournalpost());
            documentService.createJournalPost(tilskuddFartoyNew);
            return tilskuddFartoyNew;


        } else {
            throw new CreateTilskuddFartoyException(caseOperationResult.getErrorDetails().getValue());
        }

    }

    public TilskuddFartoyResource getTilskuddFartoyCaseByCaseNumber(String caseNumber) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setCaseNumber(objectFactory.createGetCasesQueryCaseNumber(caseNumber));
        log.info("Query: {}", getCasesQuery);
        return tilskuddFartoyFactory.toFintResource(getCase(getCasesQuery));

    }

    public TilskuddFartoyResource getTilskuddFartoyCaseBySystemId(String systemId) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setRecno(objectFactory.createGetCasesQueryRecno(Integer.valueOf(systemId)));
        log.info("Query: {}", getCasesQuery);
        return tilskuddFartoyFactory.toFintResource(getCase(getCasesQuery));

    }

    public Stream<TilskuddFartoyResource> searchTilskuddFartoyCaseByTitle(String query) {
        return tilskuddFartoyFactory.toFintResourceList(getCases(getGetCasesQueryByTitle(query)));
    }

    public SakResource getSakByCaseNumber(String caseNumber) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setCaseNumber(objectFactory.createGetCasesQueryCaseNumber(caseNumber));
        getCasesQuery.setIncludeCustomFields(Boolean.TRUE);
        log.info("Query: {}", getCasesQuery);
        return sakFactory.toFintResource(getCase(getCasesQuery));
    }

    public SakResource getSakBySystemId(String systemId) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setRecno(objectFactory.createGetCasesQueryRecno(Integer.valueOf(systemId)));
        log.info("Query: {}", getCasesQuery);
        return sakFactory.toFintResource(getCase(getCasesQuery));
    }

    public Stream<SakResource> searchSakByTitle(String query) {
        return sakFactory.toFintResourceList(getCases(getGetCasesQueryByTitle(query)));
    }

    private GetCasesQuery getGetCasesQueryByTitle(String query) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();

        MultiValueMap<String, String> params =
                UriComponentsBuilder.fromUriString(query).build().getQueryParams();
        getCasesQuery.setTitle(objectFactory.createGetCasesQueryTitle(String.format("%%%s%%", params.getFirst("title"))));
        getCasesQuery.setMaxReturnedCases(objectFactory.createGetCasesQueryMaxReturnedCases(Integer.valueOf(params.getFirst("maxResult"))));
        getCasesQuery.setIncludeCustomFields(Boolean.TRUE);
        return getCasesQuery;
    }

    private List<CaseResult> getCases(GetCasesQuery casesQuery) {
        GetCasesResult cases = caseServicePort.getCases(casesQuery);

        if (cases.isSuccessful() && cases.getTotalPageCount().getValue() > 0) {
            return cases.getCases().getValue().getCaseResult();
        }
        if (cases.getTotalPageCount().getValue() != 1) {
            throw new GetTilskuddFartoyNotFoundException("Case could not be found");
        }
        throw new GetTilskuddFartoyException(cases.getErrorDetails().getValue());
    }

    private CaseResult getCase(GetCasesQuery casesQuery) {
        GetCasesResult cases = caseServicePort.getCases(casesQuery);
        log.info("Cases: {}", cases);

        if (cases.isSuccessful() && cases.getTotalPageCount().getValue() == 1) {
            return cases.getCases().getValue().getCaseResult().get(0);
        }
        if (cases.getTotalPageCount().getValue() != 1) {
            throw new GetTilskuddFartoyNotFoundException("Case could not be found");
        }
        throw new GetTilskuddFartoyException(cases.getErrorDetails().getValue());
    }


}