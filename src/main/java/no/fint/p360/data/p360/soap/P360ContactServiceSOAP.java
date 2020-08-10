package no.fint.p360.data.p360.soap;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.contact.*;
import no.fint.p360.data.exception.CreateContactException;
import no.fint.p360.data.exception.CreateEnterpriseException;
import no.fint.p360.data.exception.EnterpriseNotFound;
import no.fint.p360.data.exception.PrivatePersonNotFound;
import no.fint.p360.data.p360.P360ContactService;
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
public class P360ContactServiceSOAP extends P360AbstractSOAPService implements P360ContactService {

    private static final QName SERVICE_NAME = new QName("http://software-innovation.com/SI.Data", "ContactService");

    private IContactService contactService;
    private ObjectFactory objectFactory;

    @Value("${fint.p360.wsdl-location:./src/main/resources/wsdl}/ContactService.wsdl")
    private String wsdlLocation;

    public P360ContactServiceSOAP() {
        super("http://software-innovation.com/SI.Data", "ContactService");
    }

    @PostConstruct
    private void init() throws MalformedURLException {
        URL wsdlLocationUrl = P360Utils.getURL(wsdlLocation);
        log.info("WSDL location: {}", wsdlLocationUrl);
        contactService = new ContactService(wsdlLocationUrl, SERVICE_NAME).getBasicHttpBindingIContactService();
        super.setup(contactService, "ContactService");

        objectFactory = new ObjectFactory();
    }

    @Override public PrivatePersonResult getPrivatePersonByRecno(int recNo) {
        GetPrivatePersonsParameter getPrivatePersonsParameter = new GetPrivatePersonsParameter();
        getPrivatePersonsParameter.setIncludeCustomFields(Boolean.TRUE);
        getPrivatePersonsParameter.setRecno(objectFactory.createGetPrivatePersonsParameterRecno(recNo));
        GetPrivatePersonsResult privatePersons = contactService.getPrivatePersons(getPrivatePersonsParameter);

        log.info("PrivatePersonsResult: {}", privatePersons);

        if (privatePersons.isSuccessful() && privatePersons.getTotalPageCount().getValue() == 1) {
            return privatePersons.getPrivatePersons().getValue().getPrivatePersonResult().get(0);
        }
        return null;
    }

    @Override public PrivatePersonResult getPrivatePersonByPersonalIdNumber(String personalIdNumber) throws PrivatePersonNotFound {
        GetPrivatePersonsParameter getPrivatePersonsParameter = objectFactory.createGetPrivatePersonsParameter();
        getPrivatePersonsParameter.setIncludeCustomFields(Boolean.TRUE);

        getPrivatePersonsParameter.setPersonalIdNumber(
                objectFactory.createGetPrivatePersonsParameterPersonalIdNumber(personalIdNumber));

        GetPrivatePersonsResult privatePersons = contactService.getPrivatePersons(getPrivatePersonsParameter);

        log.info("PrivatePersonsResult: {}", privatePersons);

        if (privatePersons.isSuccessful() && privatePersons.getTotalPageCount().getValue() == 1) {
            return privatePersons.getPrivatePersons().getValue().getPrivatePersonResult().get(0);
        }

        throw new PrivatePersonNotFound(privatePersons.getErrorMessage().getValue());
    }

    @Override public ContactPersonResult getContactPersonByRecno(int recNo) {

        GetContactPersonsParameter getContactPersonsParameter = new GetContactPersonsParameter();
        getContactPersonsParameter.setIncludeCustomFields(Boolean.TRUE);
        getContactPersonsParameter.setRecno(objectFactory.createGetContactPersonsParameterRecno(recNo));
        GetContactPersonsResult contactPersons = contactService.getContactPersons(getContactPersonsParameter);

        log.info("ContactPersonsResult: {}", contactPersons);

        if (contactPersons.isSuccessful() && contactPersons.getTotalPageCount().getValue() == 1) {
            return contactPersons.getContactPersons().getValue().getContactPersonResult().get(0);
        }
        return null;
    }

    @Override public EnterpriseResult getEnterpriseByRecno(int recNo) {

        GetEnterprisesParameter getEnterpriseParameter = new GetEnterprisesParameter();
        getEnterpriseParameter.setIncludeCustomFields(Boolean.TRUE);
        getEnterpriseParameter.setRecno(objectFactory.createGetEnterprisesParameterRecno(recNo));
        GetEnterprisesResult enterprises = contactService.getEnterprises(getEnterpriseParameter);

        log.info("EnterpriseResult: {}", enterprises);

        if (enterprises.isSuccessful() && enterprises.getTotalPageCount().getValue() == 1) {
            return enterprises.getEnterprises().getValue().getEnterpriseResult().get(0);
        }

        return null;
    }

    @Override public EnterpriseResult getEnterpriseByEnterpriseNumber(String enterpriseNumber) throws EnterpriseNotFound {
        GetEnterprisesParameter getEnterprisesParameter = objectFactory.createGetEnterprisesParameter();
        getEnterprisesParameter.setIncludeCustomFields(Boolean.TRUE);
        getEnterprisesParameter.setEnterpriseNumber(objectFactory.createGetEnterprisesParameterEnterpriseNumber(enterpriseNumber));

        GetEnterprisesResult enterprises = contactService.getEnterprises(getEnterprisesParameter);

        log.info("EnterpriseResult: {}", enterprises);

        if (enterprises.isSuccessful() && enterprises.getTotalPageCount().getValue() == 1) {
            return enterprises.getEnterprises().getValue().getEnterpriseResult().get(0);
        }

        throw new EnterpriseNotFound(enterprises.getErrorMessage().getValue());
    }

    @Override public boolean ping() {

        try {
            contactService.ping();
        } catch (WebServiceException e) {
            return false;
        }

        return true;
    }

    @Override public Stream<EnterpriseResult> searchEnterprise(Map<String, String> queryParams) {
        GetEnterprisesParameter getEnterprisesParameter = objectFactory.createGetEnterprisesParameter();
        if (queryParams.containsKey("navn")) {
            getEnterprisesParameter.setName(objectFactory.createGetEnterprisesParameterName(queryParams.get("navn")));
        }
        if (queryParams.containsKey("organisasjonsnummer")) {
            getEnterprisesParameter.setEnterpriseNumber(objectFactory.createGetEnterprisesParameterEnterpriseNumber(queryParams.get("organisasjonsnummer")));
        }
        if (queryParams.containsKey("maxResults")) {
            getEnterprisesParameter.setMaxRows(objectFactory.createGetEnterprisesParameterMaxRows(Integer.valueOf(queryParams.get("maxResults"))));
        }

        log.info("GetEnterprises query: {}", getEnterprisesParameter);
        GetEnterprisesResult result = contactService.getEnterprises(getEnterprisesParameter);
        log.info("GetEnterprises result: {}", result);

        if (!result.isSuccessful()) {
            return Stream.empty();
        }

        return result.getEnterprises().getValue().getEnterpriseResult().stream();
    }

    @Override public Stream<PrivatePersonResult> searchPrivatePerson(Map<String, String> queryParams) {
        GetPrivatePersonsParameter getPrivatePersonsParameter = objectFactory.createGetPrivatePersonsParameter();
        if (queryParams.containsKey("navn")) {
            getPrivatePersonsParameter.setName(objectFactory.createGetPrivatePersonsParameterName(queryParams.get("navn")));
        }
        if (queryParams.containsKey("maxResults")) {
            getPrivatePersonsParameter.setMaxRows(objectFactory.createGetPrivatePersonsParameterMaxRows(Integer.valueOf(queryParams.get("maxResults"))));
        }

        log.info("GetPrivatePersons query: {}", getPrivatePersonsParameter);
        GetPrivatePersonsResult result = contactService.getPrivatePersons(getPrivatePersonsParameter);
        log.info("GetPrivatePersons: {}", result);

        if (!result.isSuccessful()) {
            return Stream.empty();
        }

        return result.getPrivatePersons().getValue().getPrivatePersonResult().stream();
    }

    @Override public Stream<ContactPersonResult> searchContactPerson(Map<String, String> queryParams) {
        GetContactPersonsParameter getContactPersonsParameter = objectFactory.createGetContactPersonsParameter();

        if (queryParams.containsKey("navn")) {
            getContactPersonsParameter.setName(objectFactory.createGetPrivatePersonsParameterName(queryParams.get("navn")));
        }
        if (queryParams.containsKey("maxResults")) {
            getContactPersonsParameter.setMaxRows(objectFactory.createGetPrivatePersonsParameterMaxRows(Integer.valueOf(queryParams.get("maxResults"))));
        }

        log.info("GetContactPersons query: {}", getContactPersonsParameter);
        GetContactPersonsResult result = contactService.getContactPersons(getContactPersonsParameter);
        log.info("GetContactPersons result: {}", result);

        if (!result.isSuccessful()) {
            return Stream.empty();
        }

        return result.getContactPersons().getValue().getContactPersonResult().stream();
    }

    @Override public Integer createPrivatePerson(SynchronizePrivatePersonParameter privatePerson) throws CreateContactException {
        log.info("Create Private Person: {}", privatePerson);
        SynchronizePrivatePersonResult privatePersonResult = contactService.synchronizePrivatePerson(privatePerson);
        log.info("Private Person Result: {}", privatePersonResult);
        if (privatePersonResult.isSuccessful()) {
            return privatePersonResult.getRecno();
        }
        throw new CreateContactException(privatePersonResult.getErrorMessage().getValue());
    }

    @Override public Integer createEnterprise(SynchronizeEnterpriseParameter enterprise) throws CreateEnterpriseException {
        log.info("Create Enterprise: {}", enterprise);
        SynchronizeEnterpriseResult result = contactService.synchronizeEnterprise(enterprise);
        log.info("Enterprise Result: {}", result);
        if (result.isSuccessful()) {
            return result.getRecno();
        }
        throw new CreateEnterpriseException(result.getErrorMessage().getValue());
    }

}

