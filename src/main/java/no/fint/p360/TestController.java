package no.fint.p360;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.fint.arkiv.p360.accessgroup.GetAccessGroupsQuery;
import no.fint.arkiv.p360.user.ArrayOfUserProfile;
import no.fint.arkiv.p360.user.ObjectFactory;
import no.fint.arkiv.p360.user.SynchronizeUserParameter;
import no.fint.arkiv.p360.user.UserProfile;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.p360.data.p360.P360AccessGroupService;
import no.fint.p360.data.p360.P360SupportService;
import no.fint.p360.data.p360.P360UserService;
import no.fint.p360.service.EventHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    @Autowired
    private P360SupportService supportService;

    @Autowired
    private P360AccessGroupService accessGroupService;

    @Autowired
    private P360UserService userService;

    @Autowired
    private AdapterProps adapterProps;

    @Autowired
    private EventHandlerService eventHandlerService;

    @PostMapping
    public Event<FintLinks> handleEvent(@RequestBody Event<FintLinks> input) {
        Event<FintLinks> response = new Event<>(input);
        eventHandlerService.getActionsHandlerMap().getOrDefault(input.getAction(), r -> r.setResponseStatus(ResponseStatus.REJECTED)).accept(response);
        return response;
    }

    @GetMapping(value = "codelist", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getCodelist(@RequestParam String id) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(supportService.getCodeTable(id));
    }

    @GetMapping(value = "accessgroup", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAccessGroup() throws JsonProcessingException {
        final no.fint.arkiv.p360.accessgroup.ObjectFactory factory = new no.fint.arkiv.p360.accessgroup.ObjectFactory();
        GetAccessGroupsQuery parameter = factory.createGetAccessGroupsQuery();
        return new ObjectMapper().writeValueAsString(accessGroupService.getAccessGroups(parameter));
    }

    @PostMapping(value = "synchronizeuser", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer synchronizeUser() {
        ObjectFactory factory = new ObjectFactory();
        SynchronizeUserParameter parameter = factory.createSynchronizeUserParameter();
        parameter.setLogin(factory.createUserBaseLogin(adapterProps.getP360User()));
        parameter.setContactExternalId(factory.createUserBaseContactExternalId("recno:225958"));
        ArrayOfUserProfile profiles = factory.createArrayOfUserProfile();
        profiles.getUserProfile().add(createUserProfile(factory,"recno:1", "recno:100001"));
        profiles.getUserProfile().add(createUserProfile(factory, "recno:4", "recno:100001"));
        parameter.setProfiles(factory.createUserBaseProfiles(profiles));
        return userService.synchronizeUser(parameter);
    }

    private UserProfile createUserProfile(ObjectFactory factory, String role, String enterpriseId) {
        UserProfile userProfile = factory.createUserProfile();
        userProfile.setRole(factory.createUserProfileRole(role));
        userProfile.setEnterpriseId(factory.createUserProfileEnterpriseId(enterpriseId));
        return userProfile;
    }
}
