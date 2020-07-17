package no.fint.p360.data.noark.autorisasjon;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.accessgroup.ArrayOfAccessGroupResult;
import no.fint.model.resource.administrasjon.arkiv.AutorisasjonResource;
import no.fint.p360.data.p360.P360AccessGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@Slf4j
public class AutorisasjonService {

    @Autowired
    private P360AccessGroupService accessGroupService;

    @Autowired
    private AutorisasjonFactory autorisasjonFactory;

    public Stream<AutorisasjonResource> getAutoriasjon() {
        ArrayOfAccessGroupResult accessGroups = accessGroupService.getAccessGroups();
        return accessGroups.getAccessGroupResult().stream().map(autorisasjonFactory::toFintResource);
    }

    public boolean health() {
        return accessGroupService.ping();
    }
}
