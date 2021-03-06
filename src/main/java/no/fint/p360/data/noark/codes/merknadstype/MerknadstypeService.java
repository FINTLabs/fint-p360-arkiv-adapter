package no.fint.p360.data.noark.codes.merknadstype;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.MerknadstypeResource;
import no.fint.p360.data.p360.P360SupportService;
import no.fint.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class MerknadstypeService {

    @Autowired
    private P360SupportService supportService;

    @Value("${fint.p360.tables.note-type:code table: Note type}")
    private String codeTableName;

    public Stream<MerknadstypeResource> getMerknadstype() {
        return supportService.getCodeTableRowResultStream(codeTableName)
                .map(BegrepMapper.mapValue(MerknadstypeResource::new));
    }
}
