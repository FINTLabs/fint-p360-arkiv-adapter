package no.fint.ra.data.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.*;
import no.fint.ra.data.exception.CreateCaseException;
import no.fint.ra.data.exception.GetTilskuddFartoyException;
import no.fint.ra.data.exception.GetTilskuddFartoyNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;
import java.util.List;

@Service
@Slf4j
public class P360CaseService extends P360AbstractService {


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


    public String createCase(CreateCaseParameter createCaseParameter) {
        CaseOperationResult operationResult = caseServicePort.createCase(createCaseParameter);

        if (operationResult.isSuccessful()) {
            return operationResult.getCaseNumber().getValue();
        } else {
            throw new CreateCaseException(operationResult.getErrorDetails().getValue());
        }
    }

    public CaseResult getSakByCaseNumber(String caseNumber) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setCaseNumber(objectFactory.createGetCasesQueryCaseNumber(caseNumber));
        getCasesQuery.setIncludeCustomFields(Boolean.TRUE);
        log.info("Query: {}", getCasesQuery);
        return getCase(getCasesQuery);
    }

    public CaseResult getSakBySystemId(String systemId) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setRecno(objectFactory.createGetCasesQueryRecno(Integer.valueOf(systemId)));
        log.info("Query: {}", getCasesQuery);
        return getCase(getCasesQuery);
    }


    public List<CaseResult> getGetCasesQueryByTitle(MultiValueMap<String, String> params) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setTitle(objectFactory.createGetCasesQueryTitle(String.format("%%%s%%", params.getFirst("title"))));
        getCasesQuery.setMaxReturnedCases(objectFactory.createGetCasesQueryMaxReturnedCases(Integer.valueOf(params.getFirst("maxResult"))));
        getCasesQuery.setIncludeCustomFields(Boolean.TRUE);
        return getCases(getCasesQuery);
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