package no.fint.p360.data.noark.codes;

import no.fint.arkiv.p360.support.CodeTableResult;
import no.fint.p360.data.p360.P360SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.stream.Stream;

@Service
public class CaseCategoryService {
    @Autowired
    private P360SupportService supportService;

    @Value("${fint.p360.tables.document-status:code table: Case category}")
    private String documentStatusTable;

    public Stream<String> getCaseCategoryTable() {
        return supportService
                .getCodeTableRowResultStream(documentStatusTable)
                .map(CodeTableResult::getDescription)
                .map(JAXBElement::getValue);
    }
}
