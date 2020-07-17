package no.fint.p360.data.noark.arkivressurs;

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

    public Stream<ArkivressursResource> getArkivressurs() {
        ArrayOfUserBase users = userService.getUsers();
        return users.getUserBase().stream().map(arkivressursFactory::toFintResource);
    }

    public boolean health() {
        return userService.ping();
    }
}
