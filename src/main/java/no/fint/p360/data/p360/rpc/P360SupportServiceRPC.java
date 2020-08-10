package no.fint.p360.data.p360.rpc;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.arkiv.p360.support.GetCodeTableRowsResult;
import no.fint.p360.data.exception.CodeTableNotFound;
import no.fint.p360.data.p360.P360SupportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.p360.api", havingValue = "RPC")
public class P360SupportServiceRPC extends P360AbstractRPCService implements P360SupportService {
    @Override
    public GetCodeTableRowsResult getCodeTable(String table) throws CodeTableNotFound {
        return null;
    }

    @Override
    public Stream<CodeTableRowResult> getCodeTableRowResultStream(String table) {
        return Stream.empty();
    }

    @Override
    public boolean ping() {
        final Map result = call("/SupportService/Ping", "", Map.class);
        return result != null;
    }

    @Override
    public String getSIFVersion() {
        return call("/SupportService/GetSIFVersion", "", String.class);
    }
}
