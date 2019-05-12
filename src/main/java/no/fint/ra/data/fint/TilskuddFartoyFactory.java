package no.fint.ra.data.fint;

import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.arkiv.p360.caze.CreateCaseParameter;
import no.fint.arkiv.p360.caze.ObjectFactory;
import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.SaksstatusResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.KulturminneProps;
import no.fint.ra.data.noark.NoarkFactory;
import no.fint.ra.data.utilities.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
public class TilskuddFartoyFactory {


    @Autowired
    private KodeverkService kodeverkService;

    @Autowired
    private NoarkFactory noarkFactory;

    @Autowired
    private KulturminneProps kulturminneProps;

    private ObjectFactory objectFactory;


    @PostConstruct
    private void init() {
        objectFactory = new ObjectFactory();
    }

    public TilskuddFartoyResource toFintResource(CaseResult caseResult) {

        TilskuddFartoyResource tilskuddFartoy = new TilskuddFartoyResource();
        String caseNumber = caseResult.getCaseNumber().getValue();

        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

        // FIXME
        tilskuddFartoy.setFartoyNavn("Titanic");
        tilskuddFartoy.setKallesignal("AWQR");
        tilskuddFartoy.setKulturminneId("1234");
        tilskuddFartoy.setSoknadsnummer(FintUtils.createIdentifikator("1"));

        noarkFactory.getSaksmappe(caseResult, tilskuddFartoy);

        FintUtils.optionalValue(caseResult.getStatus())
                .flatMap(kode -> kodeverkService
                        .getSaksstatus()
                        .stream()
                        .filter(it -> StringUtils.equalsIgnoreCase(kode, it.getNavn()))
                        .findAny())
                .map(SaksstatusResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(Saksstatus.class, "systemid"))
                .ifPresent(tilskuddFartoy::addSaksstatus);
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));

        return tilskuddFartoy;
    }


    public Stream<TilskuddFartoyResource> toFintResourceList(List<CaseResult> caseResult) {
        return caseResult
                .stream()
                .map(this::toFintResource);
    }

    public CreateCaseParameter toP360(TilskuddFartoyResource tilskuddFartoy) {
        CreateCaseParameter createCaseParameter = new CreateCaseParameter();

        createCaseParameter.setTitle(objectFactory.createCaseParameterBaseTitle(TitleParser.getTitleString(tilskuddFartoy)));
        createCaseParameter.setStatus(objectFactory.createCaseParameterBaseStatus(kulturminneProps.getInitialCaseStatus()));
        createCaseParameter.setFiledOnPaper(objectFactory.createCaseParameterBaseFiledOnPaper(false));
        createCaseParameter.setKeywords(P360Utils.getKeywords(Arrays.asList(kulturminneProps.getKeywords())));
        createCaseParameter.setCaseType(objectFactory.createCreateCaseParameterCaseType(Constants.CASE_TYPE_NOARK));
        createCaseParameter.setResponsibleEnterpriseRecno(objectFactory.createCaseParameterBaseResponsibleEnterpriseRecno(kulturminneProps.getResponsibleUnit()));
        createCaseParameter.setSubArchive(objectFactory.createCaseParameterBaseSubArchive(kulturminneProps.getSubArchive()));
        createCaseParameter.setExternalId(P360Utils.getExternalIdParameter(tilskuddFartoy.getSoknadsnummer().getIdentifikatorverdi()));
        createCaseParameter.setArchiveCodes(P360Utils.getArchiveCodes(tilskuddFartoy.getFartoyNavn(), kulturminneProps.getArchiveCodetype()));

        /*
        createCaseParameter.setResponsiblePersonIdNumber(
                objectFactory.createCaseParameterBaseResponsiblePersonIdNumber(
                        tilskuddFartoy.getSaksansvarlig().get(0).getHref()
                )
        );
        */

        return createCaseParameter;
    }


}
