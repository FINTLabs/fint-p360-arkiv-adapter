package no.fint.p360.data.p360.rpc;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.contact.*;
import no.fint.p360.data.exception.CreateContactException;
import no.fint.p360.data.exception.CreateEnterpriseException;
import no.fint.p360.data.exception.EnterpriseNotFound;
import no.fint.p360.data.exception.PrivatePersonNotFound;
import no.fint.p360.data.p360.P360ContactService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.p360.api", havingValue = "RPC")
public class P360ContactServiceRPC extends P360AbstractRPCService implements P360ContactService {
    @Override
    public PrivatePersonResult getPrivatePersonByRecno(int recNo) {
        return null;
    }

    @Override
    public PrivatePersonResult getPrivatePersonByPersonalIdNumber(String personalIdNumber) throws PrivatePersonNotFound {
        return null;
    }

    @Override
    public ContactPersonResult getContactPersonByRecno(int recNo) {
        return null;
    }

    @Override
    public EnterpriseResult getEnterpriseByRecno(int recNo) {
        return null;
    }

    @Override
    public EnterpriseResult getEnterpriseByEnterpriseNumber(String enterpriseNumber) throws EnterpriseNotFound {
        return null;
    }

    @Override
    public boolean ping() {
        return false;
    }

    @Override
    public Stream<EnterpriseResult> searchEnterprise(Map<String, String> queryParams) {
        return null;
    }

    @Override
    public Stream<PrivatePersonResult> searchPrivatePerson(Map<String, String> queryParams) {
        return null;
    }

    @Override
    public Stream<ContactPersonResult> searchContactPerson(Map<String, String> queryParams) {
        return null;
    }

    @Override
    public Integer createPrivatePerson(SynchronizePrivatePersonParameter privatePerson) throws CreateContactException {
        return null;
    }

    @Override
    public Integer createEnterprise(SynchronizeEnterpriseParameter enterprise) throws CreateEnterpriseException {
        return null;
    }
}
