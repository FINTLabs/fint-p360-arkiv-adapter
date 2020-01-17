package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.accessgroup.*;
import no.fint.p360.data.exception.GetAccessGroupsException;
import no.fint.p360.data.utilities.P360Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
public class P360AccessGroupService extends P360AbstractService {

    private static final QName SERVICE_NAME = new QName("http://software-innovation.com/SI.Data", "AccessGroupService");

    private IAccessGroupService accessGroupService;
    private ObjectFactory objectFactory;

    @Value("${fint.p360.wsdl-location:./src/main/resources/wsdl}/AccessGroupService.wsdl")
    private String wsdlLocation;

    public P360AccessGroupService() {
        super("http://software-innovation.com/SI.Data", "AccessGroupService");
    }

    @PostConstruct
    private void init() throws MalformedURLException {
        URL wsdlLocationUrl = P360Utils.getURL(wsdlLocation);
        log.info("WSDL location: {}", wsdlLocationUrl);
        accessGroupService = new AccessGroupService(wsdlLocationUrl, SERVICE_NAME).getBasicHttpBindingIAccessGroupService();
        super.setup(accessGroupService, "AccessGroupService");
        objectFactory = new ObjectFactory();
    }

    public ArrayOfAccessGroupResult getAccessGroups() {
        GetAccessGroupsQuery parameter = objectFactory.createGetAccessGroupsQuery();
        GetAccessGroupsResult getAccessGroupsResult = accessGroupService.getAccessGroups(parameter);
        if (getAccessGroupsResult.isSuccessful()) {
            return getAccessGroupsResult.getAccessGroups().getValue();
        }
        throw new GetAccessGroupsException(getAccessGroupsResult.getErrorMessage().getValue());
    }

    public boolean ping() {

        try {
            accessGroupService.ping();
        } catch (WebServiceException e) {
            return false;
        }

        return true;
    }

}
