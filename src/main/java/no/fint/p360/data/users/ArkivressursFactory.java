package no.fint.p360.data.users;

import no.fint.arkiv.p360.user.GetUsersParameter;
import no.fint.arkiv.p360.user.ObjectFactory;
import no.fint.arkiv.p360.user.UserBase;
import no.fint.model.resource.administrasjon.arkiv.ArkivressursResource;
import no.fint.p360.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@Service
public class ArkivressursFactory {

    private ObjectFactory objectFactory = new ObjectFactory();

    public GetUsersParameter createGetUsersParameterFromSystemId(String systemId) {
        GetUsersParameter getUsersParameter = objectFactory.createGetUsersParameter();
        //getUsersParameter.setUserId(objectFactory.createGetUsersParameterUserId(systemId));
        return getUsersParameter;
    }

    public ArkivressursResource toFintResource(UserBase userBase) {
        ArkivressursResource arkivressurs = new ArkivressursResource();
        optionalValue(userBase.getLogin())
                .map(FintUtils::createIdentifikator)
                .ifPresent(arkivressurs::setSystemId);
        optionalValue(userBase.getContactExternalId())
                .filter(s -> !StringUtils.startsWith(s, "recno:"))
                .map(FintUtils::createIdentifikator)
                .ifPresent(arkivressurs::setKildesystemId);
        return arkivressurs;
    }
}
