package no.fint.p360.data.noark.part;

import no.fint.arkiv.p360.caze.CaseContactResult;
import no.fint.arkiv.p360.contact.ContactPersonResult;
import no.fint.arkiv.p360.contact.EnterpriseResult;
import no.fint.arkiv.p360.contact.PrivatePersonResult;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.PartResource;
import no.fint.model.resource.administrasjon.arkiv.PartRolleResource;
import no.fint.model.resource.administrasjon.arkiv.PartsinformasjonResource;
import no.fint.p360.data.KodeverkRepository;
import no.fint.p360.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@SuppressWarnings("Duplicates")
@Service
public class PartFactory {

    @Autowired
    KodeverkRepository kodeverkRepository;

    public PartResource toFintResource(PrivatePersonResult result) {

        if (result == null) {
            return null;
        }

        PartResource partResource = new PartResource();
        partResource.setAdresse(FintUtils.createAdresse(result));
        partResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        partResource.setPartNavn(FintUtils.getFullNameString(result));
        partResource.setPartId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return partResource;
    }

    public PartResource toFintResource(ContactPersonResult result) {

        if (result == null) {
            return null;
        }

        PartResource partResource = new PartResource();
        partResource.setAdresse(FintUtils.createAdresse(result));
        partResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        partResource.setPartNavn(FintUtils.getFullNameString(result));
        partResource.setPartId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return partResource;
    }

    public PartResource toFintResource(EnterpriseResult result) {

        if (result == null) {
            return null;
        }

        PartResource partResource = new PartResource();
        partResource.setAdresse(FintUtils.createAdresse(result));
        partResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        partResource.setPartNavn(result.getName().getValue());
        partResource.setKontaktperson(FintUtils.getKontaktpersonString(result));
        partResource.setPartId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return partResource;

    }

    public PartsinformasjonResource getPartsinformasjon(CaseContactResult caseContactResult) {
        PartsinformasjonResource partsinformasjonResource = new PartsinformasjonResource();

        optionalValue(caseContactResult.getRecno())
                .map(String::valueOf)
                .map(Link.apply(PartResource.class, "partid"))
                .ifPresent(partsinformasjonResource::addPart);

        optionalValue(caseContactResult.getRole())
                .flatMap(role ->
                    kodeverkRepository
                            .getPartRolle()
                            .stream()
                            .filter(v -> StringUtils.equalsIgnoreCase(role, v.getKode()))
                            .findAny())
                .map(PartRolleResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(PartRolleResource.class, "systemid"))
                .ifPresent(partsinformasjonResource::addPartRolle);

        return partsinformasjonResource;
    }


}
