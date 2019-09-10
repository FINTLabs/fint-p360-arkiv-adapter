package no.fint.p360.data.noark.variantformat;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.VariantformatResource;
import no.fint.p360.data.p360.P360SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class VariantformatService {

    @Autowired
    private P360SupportService supportService;

    @Autowired
    private VariantformatFactory factory;

    @Value("${fint.p360.tables.document-relation:Attribute value: File - ToVersionFormat}")
    private String fileStatusTable;

    public Stream<VariantformatResource> getFileStatusTable() {
        return supportService.getCodeTableRowResultStream(fileStatusTable)
                .map(factory::toFintResource);
    }

}
