package no.fint.ra.data.fint;

import no.fint.arkiv.p360.caze.*;
import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.SaksstatusResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.KulturminneProps;
import no.fint.ra.data.noark.NoarkFactory;
import no.fint.ra.data.utilities.Constants;
import no.fint.ra.data.utilities.FintUtils;
import no.fint.ra.data.utilities.NOARKUtils;
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

        createCaseParameter.setTitle(objectFactory.createCaseParameterBaseTitle(tilskuddFartoy.getTittel()));
        createCaseParameter.setStatus(objectFactory.createCaseParameterBaseStatus("B"));
        createCaseParameter.setFiledOnPaper(objectFactory.createCaseParameterBaseFiledOnPaper(false));

        createCaseParameter.setKeywords(objectFactory.createCaseParameterBaseKeywords(getKeywords()));

        // Set default to NOARK Sak
        createCaseParameter.setCaseType(objectFactory.createCreateCaseParameterCaseType(Constants.CASE_TYPE_NOARK));

        // TODO: 2019-04-30 Denne bør vel egentlig komme fra journalEnhet
        createCaseParameter.setResponsibleEnterpriseRecno(objectFactory.createCaseParameterBaseResponsibleEnterpriseRecno(Integer.valueOf(kulturminneProps.getResponsibleUnit())));
        createCaseParameter.setSubArchive(objectFactory.createCaseParameterBaseSubArchive(kulturminneProps.getSubArchive()));

        ExternalIdParameter externalIdParameter = objectFactory.createExternalIdParameter();
        externalIdParameter.setId(objectFactory.createExternalIdParameterId(tilskuddFartoy.getSoknadsnummer().getIdentifikatorverdi()));
        externalIdParameter.setType(objectFactory.createExternalIdParameterType(Constants.EXTERNAL_ID_TYPE));

        createCaseParameter.setExternalId(objectFactory.createCaseParameterBaseExternalId(externalIdParameter));
        ArrayOfClassCodeParameter arrayOfClassCodeParameter = objectFactory.createArrayOfClassCodeParameter();
        ClassCodeParameter classCodeParameter = objectFactory.createClassCodeParameter();

        classCodeParameter.setSort(1);
        classCodeParameter.setIsManualText(Boolean.FALSE);
        classCodeParameter.setArchiveCode(objectFactory.createClassCodeParameterArchiveCode(tilskuddFartoy.getFartoyNavn()));
        classCodeParameter.setArchiveType(objectFactory.createClassCodeParameterArchiveType("Fartøy"));
        arrayOfClassCodeParameter.getClassCodeParameter().add(classCodeParameter);

        createCaseParameter.setArchiveCodes(objectFactory.createCaseParameterBaseArchiveCodes(arrayOfClassCodeParameter));

        /*
        ArrayOfRemark arrayOfRemark = objectFactory.createArrayOfRemark();
        Remark remark = new Remark();
        remark.setRemarkType(objectFactory.createString("SI"));
        remark.setContent(objectFactory.createString("test"));
        arrayOfRemark.getRemark().add(remark);
        createCaseParameter.setRemarks(objectFactory.createArrayOfRemark(arrayOfRemark));
        */
        /*
        createCaseParameter.setResponsiblePersonIdNumber(
                objectFactory.createCaseParameterBaseResponsiblePersonIdNumber(
                        tilskuddFartoy.getSaksansvarlig().get(0).getHref()
                )
        );
        */


        return createCaseParameter;

    }

    private ArrayOfstring getKeywords() {

        ArrayOfstring keywordArray = objectFactory.createArrayOfstring();
        List<String> keywords = Arrays.asList(kulturminneProps.getKeywords());

        keywords.forEach(keywordArray.getString()::add);

        return keywordArray;
    }

}
