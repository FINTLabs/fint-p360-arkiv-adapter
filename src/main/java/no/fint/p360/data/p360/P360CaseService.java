package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.*;
import no.fint.p360.data.exception.CreateCaseException;
import no.fint.p360.data.exception.GetTilskuddFartoyException;
import no.fint.p360.data.exception.GetTilskuddFartoyNotFoundException;
import no.fint.p360.data.utilities.Constants;
import no.fint.p360.data.utilities.P360Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class P360CaseService extends P360AbstractService {


    private ICaseService caseServicePort;

    private ObjectFactory objectFactory;

    @Value("${fint.p360.wsdl-location:./src/main/resources/wsdl}/CaseService.wsdl")
    private String wsdlLocation;

    public P360CaseService() {
        super("http://software-innovation.com/SI.Data", "CaseService");
    }

    @PostConstruct
    public void init() throws MalformedURLException {
        URL wsdlLocationUrl = P360Utils.getURL(wsdlLocation);
        log.info("WSDL location: {}", wsdlLocationUrl);
        caseServicePort = new CaseService(wsdlLocationUrl, serviceName).getBasicHttpBindingICaseService();
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


    public String createCase(CreateCaseParameter createCaseParameter) throws CreateCaseException {
        log.info("Create case: {}", createCaseParameter);
        CaseOperationResult operationResult = caseServicePort.createCase(createCaseParameter);
        log.info("Create case result: {}", operationResult);
        if (operationResult.isSuccessful()) {
            return operationResult.getCaseNumber();
        } else {
            throw new CreateCaseException(operationResult.getErrorDetails());
        }
    }

    public CaseResult getSakByCaseNumber(String caseNumber) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setCaseNumber(caseNumber);
        return getCase(getCasesQuery);
    }

    public CaseResult getSakBySystemId(String systemId) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setRecno(Integer.valueOf(systemId));
        return getCase(getCasesQuery);
    }

    public CaseResult getSakByExternalId(String externalId) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException {
        GetCasesQuery getCasesQuery = objectFactory.createGetCasesQuery();
        ExternalIdParameter externalIdParameter = objectFactory.createExternalIdParameter();
        externalIdParameter.setType(Constants.EXTERNAL_ID_TYPE);
        externalIdParameter.setId(externalId);
        getCasesQuery.setExternalId(externalIdParameter);
        return getCase(getCasesQuery);
    }

    public List<CaseResult> getGetCasesQueryByTitle(Map<String, String> params) throws GetTilskuddFartoyException {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setTitle(String.format("%%%s%%", params.get("title")));
        getCasesQuery.setMaxReturnedCases(Integer.valueOf(params.getOrDefault("maxResult", "10")));
        getCasesQuery.setIncludeCustomFields(Boolean.TRUE);
        return getCases(getCasesQuery);
    }

    private List<CaseResult> getCases(GetCasesQuery casesQuery) throws GetTilskuddFartoyException {
        GetCasesResult cases = caseServicePort.getCases(casesQuery);

        if (cases.isSuccessful()) {
            return cases.getCases().getCaseResult();
        }
        throw new GetTilskuddFartoyException(cases.getErrorDetails());
    }

    private CaseResult getCase(GetCasesQuery casesQuery) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException {
        casesQuery.setIncludeCustomFields(Boolean.TRUE);
        casesQuery.setIncludeCaseContacts(Boolean.TRUE);
        log.info("Query: {}", casesQuery);
        GetCasesResult cases = caseServicePort.getCases(casesQuery);
        log.info("Cases: {}", cases);

        if (cases.isSuccessful() && cases.getTotalPageCount() == 1) {
            return cases.getCases().getCaseResult().get(0);
        }
        if (cases.getTotalPageCount() != 1) {
            throw new GetTilskuddFartoyNotFoundException("Case could not be found");
        }
        throw new GetTilskuddFartoyException(cases.getErrorDetails());
    }


}