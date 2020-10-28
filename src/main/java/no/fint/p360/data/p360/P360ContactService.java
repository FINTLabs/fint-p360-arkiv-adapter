package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.contact.*;
import no.fint.p360.data.exception.CreateContactException;
import no.fint.p360.data.exception.CreateEnterpriseException;
import no.fint.p360.data.exception.EnterpriseNotFound;
import no.fint.p360.data.exception.PrivatePersonNotFound;
import no.fint.p360.data.utilities.P360Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class P360ContactService extends P360AbstractService {

    private static final QName SERVICE_NAME = new QName("http://software-innovation.com/SI.Data", "ContactService");

    private IContactService contactService;


    @Value("${fint.p360.wsdl-location:./src/main/resources/wsdl}/ContactService.wsdl")
    private String wsdlLocation;

    public P360ContactService() {
        super("http://software-innovation.com/SI.Data", "ContactService");
    }

    @PostConstruct
    private void init() throws MalformedURLException {
        URL wsdlLocationUrl = P360Utils.getURL(wsdlLocation);
        log.info("WSDL location: {}", wsdlLocationUrl);
        contactService = new ContactService(wsdlLocationUrl, SERVICE_NAME).getBasicHttpBindingIContactService();
        super.setup(contactService, "ContactService");


    }

    public PrivatePersonResult getPrivatePersonByRecno(int recNo) {
        GetPrivatePersonsParameter getPrivatePersonsParameter = new GetPrivatePersonsParameter();
        getPrivatePersonsParameter.setIncludeCustomFields(Boolean.TRUE);
        getPrivatePersonsParameter.setRecno(recNo);
        GetPrivatePersonsResult privatePersons = contactService.getPrivatePersons(getPrivatePersonsParameter);

        log.info("PrivatePersonsResult: {}", privatePersons);

        if (privatePersons.isSuccessful() && privatePersons.getTotalPageCount() == 1) {
            return privatePersons.getPrivatePersons().getPrivatePersonResult().get(0);
        }
        return null;
    }

    public PrivatePersonResult getPrivatePersonByPersonalIdNumber(String personalIdNumber) throws PrivatePersonNotFound {
        GetPrivatePersonsParameter getPrivatePersonsParameter = new GetPrivatePersonsParameter();
        getPrivatePersonsParameter.setIncludeCustomFields(Boolean.TRUE);

        getPrivatePersonsParameter.setPersonalIdNumber(
                personalIdNumber);

        GetPrivatePersonsResult privatePersons = contactService.getPrivatePersons(getPrivatePersonsParameter);

        log.info("PrivatePersonsResult: {}", privatePersons);

        if (privatePersons.isSuccessful() && privatePersons.getTotalPageCount() == 1) {
            return privatePersons.getPrivatePersons().getPrivatePersonResult().get(0);
        }

        throw new PrivatePersonNotFound(privatePersons.getErrorMessage());
    }

    public ContactPersonResult getContactPersonByRecno(int recNo) {

        GetContactPersonsParameter getContactPersonsParameter = new GetContactPersonsParameter();
        getContactPersonsParameter.setIncludeCustomFields(Boolean.TRUE);
        getContactPersonsParameter.setRecno(recNo);
        GetContactPersonsResult contactPersons = contactService.getContactPersons(getContactPersonsParameter);

        log.info("ContactPersonsResult: {}", contactPersons);

        if (contactPersons.isSuccessful() && contactPersons.getTotalPageCount() == 1) {
            return contactPersons.getContactPersons().getContactPersonResult().get(0);
        }
        return null;
    }

    public EnterpriseResult getEnterpriseByRecno(int recNo) {

        GetEnterprisesParameter getEnterpriseParameter = new GetEnterprisesParameter();
        getEnterpriseParameter.setIncludeCustomFields(Boolean.TRUE);
        getEnterpriseParameter.setRecno(recNo);
        GetEnterprisesResult enterprises = contactService.getEnterprises(getEnterpriseParameter);

        log.info("EnterpriseResult: {}", enterprises);

        if (enterprises.isSuccessful() && enterprises.getTotalPageCount() == 1) {
            return enterprises.getEnterprises().getEnterpriseResult().get(0);
        }

        return null;
    }

    public EnterpriseResult getEnterpriseByEnterpriseNumber(String enterpriseNumber) throws EnterpriseNotFound {
        GetEnterprisesParameter getEnterprisesParameter = new GetEnterprisesParameter();
        getEnterprisesParameter.setIncludeCustomFields(Boolean.TRUE);
        getEnterprisesParameter.setEnterpriseNumber(enterpriseNumber);

        GetEnterprisesResult enterprises = contactService.getEnterprises(getEnterprisesParameter);

        log.info("EnterpriseResult: {}", enterprises);

        if (enterprises.isSuccessful() && enterprises.getTotalPageCount() == 1) {
            return enterprises.getEnterprises().getEnterpriseResult().get(0);
        }

        throw new EnterpriseNotFound(enterprises.getErrorMessage());
    }

    public boolean ping() {

        try {
            contactService.ping();
        } catch (WebServiceException e) {
            return false;
        }

        return true;
    }

    public Stream<EnterpriseResult> searchEnterprise(Map<String, String> queryParams) {
        GetEnterprisesParameter getEnterprisesParameter = new GetEnterprisesParameter();
        if (queryParams.containsKey("navn")) {
            getEnterprisesParameter.setName(queryParams.get("navn"));
        }
        if (queryParams.containsKey("organisasjonsnummer")) {
            getEnterprisesParameter.setEnterpriseNumber(queryParams.get("organisasjonsnummer"));
        }
        if (queryParams.containsKey("maxResults")) {
            getEnterprisesParameter.setMaxRows(Integer.valueOf(queryParams.get("maxResults")));
        }

        log.info("GetEnterprises query: {}", getEnterprisesParameter);
        GetEnterprisesResult result = contactService.getEnterprises(getEnterprisesParameter);
        log.info("GetEnterprises result: {}", result);

        if (!result.isSuccessful()) {
            return Stream.empty();
        }

        return result.getEnterprises().getEnterpriseResult().stream();
    }

    public Stream<PrivatePersonResult> searchPrivatePerson(Map<String, String> queryParams) {
        GetPrivatePersonsParameter getPrivatePersonsParameter = new GetPrivatePersonsParameter();
        if (queryParams.containsKey("navn")) {
            getPrivatePersonsParameter.setName(queryParams.get("navn"));
        }
        if (queryParams.containsKey("maxResults")) {
            getPrivatePersonsParameter.setMaxRows(Integer.valueOf(queryParams.get("maxResults")));
        }

        log.info("GetPrivatePersons query: {}", getPrivatePersonsParameter);
        GetPrivatePersonsResult result = contactService.getPrivatePersons(getPrivatePersonsParameter);
        log.info("GetPrivatePersons: {}", result);

        if (!result.isSuccessful()) {
            return Stream.empty();
        }

        return result.getPrivatePersons().getPrivatePersonResult().stream();
    }

    public Stream<ContactPersonResult> searchContactPerson(Map<String, String> queryParams) {
        GetContactPersonsParameter getContactPersonsParameter = new GetContactPersonsParameter();

        if (queryParams.containsKey("navn")) {
            getContactPersonsParameter.setName(queryParams.get("navn"));
        }
        if (queryParams.containsKey("maxResults")) {
            getContactPersonsParameter.setMaxRows(Integer.valueOf(queryParams.get("maxResults")));
        }

        log.info("GetContactPersons query: {}", getContactPersonsParameter);
        GetContactPersonsResult result = contactService.getContactPersons(getContactPersonsParameter);
        log.info("GetContactPersons result: {}", result);

        if (!result.isSuccessful()) {
            return Stream.empty();
        }

        return result.getContactPersons().getContactPersonResult().stream();
    }

    public Integer createPrivatePerson(SynchronizePrivatePersonParameter privatePerson) throws CreateContactException {
        log.info("Create Private Person: {}", privatePerson);
        SynchronizePrivatePersonResult privatePersonResult = contactService.synchronizePrivatePerson(privatePerson);
        log.info("Private Person Result: {}", privatePersonResult);
        if (privatePersonResult.isSuccessful()) {
            return privatePersonResult.getRecno();
        }
        throw new CreateContactException(privatePersonResult.getErrorMessage());
    }

    public Integer createEnterprise(SynchronizeEnterpriseParameter enterprise) throws CreateEnterpriseException {
        log.info("Create Enterprise: {}", enterprise);
        SynchronizeEnterpriseResult result = contactService.synchronizeEnterprise(enterprise);
        log.info("Enterprise Result: {}", result);
        if (result.isSuccessful()) {
            return result.getRecno();
        }
        throw new CreateEnterpriseException(result.getErrorMessage());
    }

}

