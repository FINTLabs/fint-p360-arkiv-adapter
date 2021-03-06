package no.fint.p360.data.noark.codes.journalstatus;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.JournalStatusResource;
import no.fint.p360.data.p360.P360SupportService;
import no.fint.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class JournalStatusService {

    @Autowired
    private P360SupportService supportService;

    @Value("${fint.p360.tables.journal-status:code table: Journal status}")
    private String journalStatusTable;

    public Stream<JournalStatusResource> getJournalStatusTable() {
        return supportService.getCodeTableRowResultStream(journalStatusTable)
                .map(BegrepMapper.mapValue(JournalStatusResource::new));
    }
}
