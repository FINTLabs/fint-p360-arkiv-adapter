package no.fint.p360.data.noark.korrespondanseparttype;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartTypeResource;
import no.fint.p360.data.p360.P360SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class KorrespondansepartTypeService {

    @Autowired
    private P360SupportService supportService;

    @Autowired
    private KorrespondansepartTypeFactory factory;

    @Value("${fint.p360.tables.contact-role:Activity - Contact role}")
    private String contactRoleTable;

    public Stream<KorrespondansepartTypeResource> getDocumentContactRole() {
        return supportService.getCodeTableRowResultStream(contactRoleTable)
                .map(factory::toFintResource);
    }
}
