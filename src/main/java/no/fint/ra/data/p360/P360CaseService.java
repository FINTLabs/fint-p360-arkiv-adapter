package no.fint.ra.data.p360;

import no.fint.arkiv.p360.caze.*;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.data.fint.TilskuddFartoyFactory;
import no.fint.ra.data.exception.CreateTilskuddFartoyException;
import no.fint.ra.data.exception.GetTilskuddFartoyException;
import no.fint.ra.data.exception.GetTilskuddFartoyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;
import java.util.List;

@Service
public class P360CaseService extends P360AbstractService {

    @Autowired
    private P360CaseFactory caseFactory;

    @Autowired
    private TilskuddFartoyFactory tilskuddFartoyFactory;

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

        /*
        tilskuddFartoy.setNoekkelord(Collections.emptyList());
        tilskuddFartoy.setSystemId(FintFactory.createIdentifikator(caseOperationResult.getRecno().toString()));
        tilskuddFartoy.setMappeId(FintFactory.createIdentifikator(caseOperationResult.getCaseNumber().getValue()));
         */


        if (caseOperationResult.isSuccessful()) {
            return getTilskuddFartoyCaseByCaseNumber(caseOperationResult.getCaseNumber().getValue());

        } else {
            throw new CreateTilskuddFartoyException(caseOperationResult.getErrorDetails().getValue());
        }

    }

    public TilskuddFartoyResource getTilskuddFartoyCaseByCaseNumber(String caseNumber) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setCaseNumber(objectFactory.createGetCasesQueryCaseNumber(caseNumber));

        return getCase(getCasesQuery);

    }

    public TilskuddFartoyResource getTilskuddFartoyCaseBySystemId(String systemId) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();
        getCasesQuery.setRecno(objectFactory.createGetCasesQueryRecno(Integer.valueOf(systemId)));

        return getCase(getCasesQuery);
    }

    public List<TilskuddFartoyResource> searchTilskuddFartoyCaseByTitle(String query) {
        GetCasesQuery getCasesQuery = new GetCasesQuery();

        MultiValueMap<String, String> params =
                UriComponentsBuilder.fromUriString(query).build().getQueryParams();
        getCasesQuery.setTitle(objectFactory.createGetCasesQueryTitle(String.format("%%%s%%", params.getFirst("title"))));
        getCasesQuery.setMaxReturnedCases(objectFactory.createGetCasesQueryMaxReturnedCases(Integer.valueOf(params.getFirst("maxResult"))));

        return getCases(getCasesQuery);
    }

    private List<TilskuddFartoyResource> getCases(GetCasesQuery casesQuery) {
        GetCasesResult cases = caseServicePort.getCases(casesQuery);

        if (cases.isSuccessful() && cases.getTotalPageCount().getValue() > 0) {
            return tilskuddFartoyFactory.p360ToFintTilskuddFartoys(cases.getCases().getValue().getCaseResult());
        }
        if (cases.getTotalPageCount().getValue() != 1) {
            throw new GetTilskuddFartoyNotFoundException("Case could not be found");
        }
        throw new GetTilskuddFartoyException(cases.getErrorDetails().getValue());
    }

    private TilskuddFartoyResource getCase(GetCasesQuery casesQuery) {

        GetCasesResult cases = caseServicePort.getCases(casesQuery);

        if (cases.isSuccessful() && cases.getTotalPageCount().getValue() == 1) {
            return tilskuddFartoyFactory.toFint(cases.getCases().getValue().getCaseResult().get(0));
        }
        if (cases.getTotalPageCount().getValue() != 1) {
            throw new GetTilskuddFartoyNotFoundException("Case could not be found");
        }
        throw new GetTilskuddFartoyException(cases.getErrorDetails().getValue());
    }


}