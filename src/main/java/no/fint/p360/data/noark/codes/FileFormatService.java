package no.fint.p360.data.noark.codes;

import no.fint.arkiv.p360.support.CodeTableResult;
import no.fint.p360.data.p360.P360SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.stream.Stream;

@Service
public class FileFormatService {
    @Autowired
    private P360SupportService supportService;

    @Value("${fint.p360.tables.file-format:code table: File Format}")
    private String fileFormatTable;

    public Stream<String> getFileFormatTable() {
        return supportService
                .getCodeTableRowResultStream(fileFormatTable)
                .map(c -> String.format("%d: %s %s", c.getRecno(), c.getCode().getValue(), c.getDescription().getValue()));
    }
}
