package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.user.*;
import no.fint.p360.data.exception.UserException;
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
public class P360UserService extends P360AbstractService {

    private static final QName SERVICE_NAME = new QName("http://software-innovation.com/SI.Data", "UserService");

    private IUserService userService;
    private ObjectFactory objectFactory;

    @Value("${fint.p360.wsdl-location:./src/main/resources/wsdl}/UserService.wsdl")
    private String wsdlLocation;

    public P360UserService() {
        super("http://software-innovation.com/SI.Data", "UserService");
    }

    @PostConstruct
    private void init() throws MalformedURLException {
        URL wsdlLocationUrl = P360Utils.getURL(wsdlLocation);
        log.info("WSDL location: {}", wsdlLocationUrl);
        userService = new UserService(wsdlLocationUrl, SERVICE_NAME).getBasicHttpBindingIUserService();
        super.setup(userService, "UserService");
        objectFactory = new ObjectFactory();
    }

    public Integer synchronizeUser(SynchronizeUserParameter parameter) {
        log.trace("synchronizeUser parameter {}", parameter);
        SynchronizeUserResult synchronizeUserResult = userService.synchronizeUser(parameter);
        log.trace("synchronizeUser result {}", synchronizeUserResult);
        if (synchronizeUserResult.isSuccessful()) {
            return synchronizeUserResult.getRecno();
        }
        throw new UserException(synchronizeUserResult.getErrorMessage().getValue());
    }

    public ArrayOfUserBase getUsers() {
        GetUsersParameter parameter = objectFactory.createGetUsersParameter();
        GetUsersResult getUsersResult = userService.getUsers(parameter);
        if (getUsersResult.isSuccessful()) {
            return getUsersResult.getUsers().getValue();
        }
        throw new UserException(getUsersResult.getErrorMessage().getValue());
    }

    public boolean ping() {

        try {
            userService.ping();
        } catch (WebServiceException e) {
            return false;
        }

        return true;
    }

}
