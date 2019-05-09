package no.fint.ra.data.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.contact.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class P360ContactService extends P360AbstractService {

    private static final QName SERVICE_NAME = new QName("http://software-innovation.com/SI.Data", "ContactService");

    private IContactService contactService;
    private ObjectFactory objectFactory;

    public P360ContactService() {
        super("http://software-innovation.com/SI.Data", "ContactService");
    }

    @PostConstruct
    private void init() {

        contactService = new ContactService(ContactService.WSDL_LOCATION, SERVICE_NAME).getBasicHttpBindingIContactService();
        super.addAuthentication(contactService);

        objectFactory = new ObjectFactory();
    }

    public PrivatePersonResult getPrivatePersonByRecno(int recNo) {
        GetPrivatePersonsParameter getPrivatePersonsParameter = new GetPrivatePersonsParameter();
        getPrivatePersonsParameter.setIncludeCustomFields(Boolean.TRUE);
        getPrivatePersonsParameter.setRecno(objectFactory.createGetPrivatePersonsParameterRecno(recNo));
        GetPrivatePersonsResult privatePersons = contactService.getPrivatePersons(getPrivatePersonsParameter);

        if (privatePersons.isSuccessful() && privatePersons.getTotalPageCount().getValue() == 1) {
            return privatePersons.getPrivatePersons().getValue().getPrivatePersonResult().get(0);
        }
        return null;
    }

    public ContactPersonResult getContactPersonByRecno(int recNo) {

        GetContactPersonsParameter getContactPersonsParameter = new GetContactPersonsParameter();
        getContactPersonsParameter.setIncludeCustomFields(Boolean.TRUE);
        getContactPersonsParameter.setRecno(objectFactory.createGetContactPersonsParameterRecno(recNo));
        GetContactPersonsResult contactPersons = contactService.getContactPersons(getContactPersonsParameter);

        if (contactPersons.isSuccessful() && contactPersons.getTotalPageCount().getValue() == 1) {
            return contactPersons.getContactPersons().getValue().getContactPersonResult().get(0);
        }
        return null;
    }

    public EnterpriseResult getEnterpriseByRecno(int recNo) {

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

    public boolean ping() {

        try {
            contactService.ping();
        } catch (WebServiceException e) {
            return false;
        }

        return true;
    }

    public Stream<EnterpriseResult> searchEnterprise(MultiValueMap<String, String> queryParams) {
        return Stream.empty();
    }

    public Stream<PrivatePersonResult> searchPrivatePerson(MultiValueMap<String, String> queryParams) {
        return Stream.empty();
    }

    public Stream<ContactPersonResult> searchContactPerson(MultiValueMap<String, String> queryParams) {
        return Stream.empty();
    }
}
