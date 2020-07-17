package no.fint.p360.data.noark.codes.rolle;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.RolleResource;
import no.fint.p360.data.p360.P360SupportService;
import no.fint.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class RolleService {

    @Autowired
    private P360SupportService supportService;

    @Value("${fint.p360.tables.case-status:Sysrole}")
    private String roleTable;

    public Stream<RolleResource> getRolle() {
        return supportService.getCodeTableRowResultStream(roleTable)
                .map(BegrepMapper.mapValue(RolleResource::new));
    }
}
