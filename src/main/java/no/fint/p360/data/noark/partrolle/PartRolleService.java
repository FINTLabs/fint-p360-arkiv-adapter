package no.fint.p360.data.noark.partrolle;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.PartRolleResource;
import no.fint.p360.data.p360.P360SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class PartRolleService {

    @Autowired
    private P360SupportService supportService;

    @Autowired
    private PartRolleFactory factory;

    @Value("${fint.p360.tables.case-contact-role:code table: Contact - Case role}")
    private String contactRoleTable;

    public Stream<PartRolleResource> getPartRolle() {
        return supportService.getCodeTableRowResultStream(contactRoleTable)
                .map(factory::toFintResource);
    }
}
