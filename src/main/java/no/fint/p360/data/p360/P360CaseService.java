package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.*;
import no.fint.p360.data.exception.CreateCaseException;
import no.fint.p360.data.exception.GetTilskuddFartoyException;
import no.fint.p360.data.exception.GetTilskuddFartoyNotFoundException;
import no.fint.p360.data.utilities.Constants;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.Map;

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
        super.setup(caseServicePort, "CaseService");

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
        log.info("Create case: {}", createCaseParameter);
        CaseOperationResult operationResult = caseServicePort.createCase(createCaseParameter);
        log.info("Create case result: {}", operationResult);
        if (operationResult.isSuccessful()) {
            return operationResult.getCaseNumber().getValue();
        } else {
            throw new CreateCaseException(operationResult.getErrorDetails().getValue());
        }
    }

    public CaseResult getSakByCaseNumber(String caseNumber) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setCaseNumber(objectFactory.createGetCasesQueryCaseNumber(caseNumber));
        return getCase(getCasesQuery);
    }

    public CaseResult getSakBySystemId(String systemId) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setRecno(objectFactory.createGetCasesQueryRecno(Integer.valueOf(systemId)));
        return getCase(getCasesQuery);
    }

    public CaseResult getSakByExternalId(String externalId) {
        GetCasesQuery getCasesQuery = objectFactory.createGetCasesQuery();
        ExternalIdParameter externalIdParameter = objectFactory.createExternalIdParameter();
        externalIdParameter.setType(objectFactory.createExternalIdParameterType(Constants.EXTERNAL_ID_TYPE));
        externalIdParameter.setId(objectFactory.createExternalIdParameterId(externalId));
        getCasesQuery.setExternalId(objectFactory.createGetCasesQueryExternalId(externalIdParameter));
        return getCase(getCasesQuery);
    }

    public List<CaseResult> getGetCasesQueryByTitle(Map<String, String> params) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setTitle(objectFactory.createGetCasesQueryTitle(String.format("%%%s%%", params.get("title"))));
        getCasesQuery.setMaxReturnedCases(objectFactory.createGetCasesQueryMaxReturnedCases(Integer.valueOf(params.get("maxResult"))));
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
        casesQuery.setIncludeCustomFields(Boolean.TRUE);
        casesQuery.setIncludeCaseContacts(Boolean.TRUE);
        log.info("Query: {}", casesQuery);
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