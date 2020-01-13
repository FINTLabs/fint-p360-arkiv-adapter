package no.fint.p360.data.users;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.user.ArrayOfUserBase;
import no.fint.arkiv.p360.user.GetUsersParameter;
import no.fint.model.resource.administrasjon.arkiv.ArkivressursResource;
import no.fint.p360.data.p360.P360UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@Slf4j
public class ArkivressursService {

    @Autowired
    private P360UserService userService;

    @Autowired
    private ArkivressursFactory arkivressursFactory;


    public Stream<ArkivressursResource> getArkivressursBySystemId(String systemId) {
        GetUsersParameter getUsersParameter = arkivressursFactory.createGetUsersParameterFromSystemId(systemId);
        ArrayOfUserBase users = userService.getUsers(getUsersParameter);
        return users.getUserBase().stream().map(arkivressursFactory::toFintResource);
    }
}
