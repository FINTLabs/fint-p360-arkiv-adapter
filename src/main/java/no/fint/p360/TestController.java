package no.fint.p360;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.fint.arkiv.p360.accessgroup.GetAccessGroupsQuery;
import no.fint.arkiv.p360.user.GetUsersParameter;
import no.fint.model.resource.administrasjon.arkiv.ArkivressursResource;
import no.fint.p360.data.p360.P360AccessGroupService;
import no.fint.p360.data.p360.P360SupportService;
import no.fint.p360.data.p360.P360UserService;
import no.fint.p360.data.users.ArkivressursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TestController {

    @Autowired
    private P360SupportService supportService;

    @Autowired
    private P360AccessGroupService accessGroupService;

    @Autowired
    private ArkivressursService arkivressursService;

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

    @GetMapping(value = "users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ArkivressursResource> getUsers(@RequestParam(required = false) String id) throws JsonProcessingException {
        return arkivressursService.getArkivressursBySystemId(id).collect(Collectors.toList());
    }

}
