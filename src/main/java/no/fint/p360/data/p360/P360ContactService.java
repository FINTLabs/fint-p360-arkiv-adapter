package no.fint.p360.data.p360;

import no.fint.arkiv.p360.contact.*;
import no.fint.p360.data.exception.CreateContactException;
import no.fint.p360.data.exception.CreateEnterpriseException;
import no.fint.p360.data.exception.EnterpriseNotFound;
import no.fint.p360.data.exception.PrivatePersonNotFound;

import java.util.Map;
import java.util.stream.Stream;

public interface P360ContactService {
    PrivatePersonResult getPrivatePersonByRecno(int recNo);

    PrivatePersonResult getPrivatePersonByPersonalIdNumber(String personalIdNumber) throws PrivatePersonNotFound;

    ContactPersonResult getContactPersonByRecno(int recNo);

    EnterpriseResult getEnterpriseByRecno(int recNo);

    EnterpriseResult getEnterpriseByEnterpriseNumber(String enterpriseNumber) throws EnterpriseNotFound;

    boolean ping();

    Stream<EnterpriseResult> searchEnterprise(Map<String, String> queryParams);

    Stream<PrivatePersonResult> searchPrivatePerson(Map<String, String> queryParams);

    Stream<ContactPersonResult> searchContactPerson(Map<String, String> queryParams);

    Integer createPrivatePerson(SynchronizePrivatePersonParameter privatePerson) throws CreateContactException;

    Integer createEnterprise(SynchronizeEnterpriseParameter enterprise) throws CreateEnterpriseException;
}
