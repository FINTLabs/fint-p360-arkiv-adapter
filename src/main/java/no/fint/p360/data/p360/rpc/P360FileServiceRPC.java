package no.fint.p360.data.p360.rpc;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.file.FileResult;
import no.fint.p360.data.p360.P360FileService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.p360.api", havingValue = "RPC")
public class P360FileServiceRPC extends P360AbstractRPCService implements P360FileService {
    @Override
    public FileResult getFileByRecNo(String recNo) {
        return null;
    }

    @Override
    public boolean ping() {
        return false;
    }
}
