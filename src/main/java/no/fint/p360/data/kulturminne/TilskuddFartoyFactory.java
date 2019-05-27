package no.fint.p360.data.kulturminne;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.*;
import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.PartsinformasjonResource;
import no.fint.model.resource.administrasjon.arkiv.SaksstatusResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.KulturminneProps;
import no.fint.p360.data.KodeverkRepository;
import no.fint.p360.data.exception.UnableToParseTitle;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.noark.korrespondansepart.KorrespondansepartFactory;
import no.fint.p360.data.utilities.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class TilskuddFartoyFactory {


    @Autowired
    private KodeverkRepository kodeverkRepository;

    @Autowired
    private NoarkFactory noarkFactory;

    @Autowired
    private KulturminneProps kulturminneProps;

    @Autowired
    private KorrespondansepartFactory korrespondansepartFactory;

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

        try {
            TitleParser.Title title = TitleParser.parseTitle(caseResult.getTitle().getValue());
            tilskuddFartoy.setFartoyNavn(Strings.nullToEmpty(title.getDimension(TitleParser.FARTOY_NAVN)));
            tilskuddFartoy.setKallesignal(Strings.nullToEmpty(title.getDimension(TitleParser.FARTOY_KALLESIGNAL)));
            tilskuddFartoy.setKulturminneId(Strings.nullToEmpty(title.getDimension(TitleParser.KULTURMINNE_ID)));
            tilskuddFartoy.setSoknadsnummer(FintUtils.createIdentifikator(caseResult.getExternalId().getValue().getId().getValue()));
        } catch (UnableToParseTitle e) {
            log.error("{}", e.getMessage(), e);
        }

        noarkFactory.getSaksmappe(caseResult, tilskuddFartoy);

        FintUtils.optionalValue(caseResult.getStatus())
                .flatMap(kode -> kodeverkRepository
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
        CreateCaseParameter createCaseParameter = objectFactory.createCreateCaseParameter();

        createCaseParameter.setTitle(objectFactory.createCaseParameterBaseTitle(TitleParser.getTitleString(tilskuddFartoy)));
        createCaseParameter.setStatus(objectFactory.createCaseParameterBaseStatus(kulturminneProps.getInitialCaseStatus()));
        createCaseParameter.setFiledOnPaper(objectFactory.createCaseParameterBaseFiledOnPaper(false));
        createCaseParameter.setKeywords(P360Utils.getKeywords(Arrays.asList(kulturminneProps.getKeywords())));
        createCaseParameter.setCaseType(objectFactory.createCreateCaseParameterCaseType(Constants.CASE_TYPE_NOARK));
        createCaseParameter.setResponsibleEnterpriseRecno(objectFactory.createCaseParameterBaseResponsibleEnterpriseRecno(kulturminneProps.getResponsibleUnit()));
        createCaseParameter.setSubArchive(objectFactory.createCaseParameterBaseSubArchive(kulturminneProps.getSubArchive()));
        createCaseParameter.setExternalId(P360Utils.getExternalIdParameter(tilskuddFartoy.getSoknadsnummer()));
        createCaseParameter.setArchiveCodes(P360Utils.getArchiveCodes(tilskuddFartoy.getFartoyNavn(), kulturminneProps.getArchiveCodetype()));

        ArrayOfCaseContactParameter arrayOfCaseContactParameter = objectFactory.createArrayOfCaseContactParameter();
//        tilskuddFartoy
//                .getPart()
//                .stream()
//                .map(this::createCaseContactParameter)
//                .forEach(arrayOfCaseContactParameter.getCaseContactParameter()::add);
//        createCaseParameter.setContacts(objectFactory.createArrayOfCaseContactParameter(arrayOfCaseContactParameter));

        /*
        createCaseParameter.setResponsiblePersonIdNumber(
                objectFactory.createCaseParameterBaseResponsiblePersonIdNumber(
                        tilskuddFartoy.getSaksansvarlig().get(0).getHref()
                )
        );
        */

        return createCaseParameter;
    }


    public CaseContactParameter createCaseContactParameter(PartsinformasjonResource partsinformasjon) {
        CaseContactParameter caseContactParameter = objectFactory.createCaseContactParameter();

        partsinformasjon
                .getPart()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(objectFactory::createCaseContactParameterReferenceNumber)
                .findFirst()
                .ifPresent(caseContactParameter::setReferenceNumber);

        partsinformasjon
                .getPartRolle()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(objectFactory::createCaseContactParameterRole)
                .findFirst()
                .ifPresent(caseContactParameter::setRole);

        return caseContactParameter;
    }

}
