package no.fint.p360.data.p360;

import no.fint.arkiv.p360.accessgroup.GetAccessGroupsQuery;
import no.fint.arkiv.p360.accessgroup.GetAccessGroupsResult;

public interface P360AccessGroupService {
    GetAccessGroupsResult getAccessGroups(GetAccessGroupsQuery parameter);

    boolean ping();
}
