package no.fint.p360.data.p360.rpc;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.accessgroup.GetAccessGroupsQuery;
import no.fint.arkiv.p360.accessgroup.GetAccessGroupsResult;
import no.fint.p360.data.p360.P360AccessGroupService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.p360.api", havingValue = "RPC")
public class P360AccessGroupServiceRPC extends P360AbstractRPCService implements P360AccessGroupService {
    @Override
    public GetAccessGroupsResult getAccessGroups(GetAccessGroupsQuery parameter) {
        return null;
    }

    @Override
    public boolean ping() {
        return false;
    }
}
