package no.fint.p360.data.kulturminne;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.*;
import no.fint.arkiv.p360.document.CreateDocumentParameter;
import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.personal.Personalressurs;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.model.resource.administrasjon.arkiv.MerknadResource;
import no.fint.model.resource.administrasjon.arkiv.PartsinformasjonResource;
import no.fint.model.resource.administrasjon.arkiv.SaksstatusResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.data.KodeverkRepository;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.exception.NoSuchTitleDimension;
import no.fint.p360.data.exception.UnableToParseTitle;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.data.noark.korrespondansepart.KorrespondansepartFactory;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.data.utilities.NOARKUtils;
import no.fint.p360.data.utilities.P360Utils;
import no.fint.p360.data.utilities.TitleParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;
import static no.fint.p360.data.utilities.P360Utils.applyParameterFromLink;

@Slf4j
@Service
public class TilskuddFartoyFactory {

    @Autowired
    private KodeverkRepository kodeverkRepository;

    @Autowired
    private NoarkFactory noarkFactory;

    @Autowired
    private KorrespondansepartFactory korrespondansepartFactory;

    @Autowired
    private JournalpostFactory journalpostFactory;

    @Autowired
    private TilskuddFartoyDefaults tilskuddFartoyDefaults;

    private ObjectFactory objectFactory;


    @PostConstruct
    private void init() {
        objectFactory = new ObjectFactory();
    }

    public TilskuddFartoyResource toFintResource(CaseResult caseResult) throws GetDocumentException, IllegalCaseNumberFormat {

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
        } catch (UnableToParseTitle | NoSuchTitleDimension e) {
            log.error("{}", e.getMessage(), e);
        }

        noarkFactory.getSaksmappe(caseResult, tilskuddFartoy);

        optionalValue(caseResult.getStatus())
                .flatMap(kode -> kodeverkRepository
                        .getSaksstatus()
                        .stream()
                        .filter(it -> StringUtils.equalsIgnoreCase(kode, it.getNavn()))
                        .findAny())
                .map(SaksstatusResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(Saksstatus.class, "systemid"))
                .ifPresent(tilskuddFartoy::addSaksstatus);

        optionalValue(caseResult.getResponsibleEnterprise())
                .map(ResponsibleEnterprise::getRecno)
                .map(String::valueOf)
                .map(Link.apply(Organisasjonselement.class, "organisasjonsid"))
                .ifPresent(tilskuddFartoy::addAdministrativEnhet);

        optionalValue(caseResult.getResponsiblePerson())
                .map(ResponsiblePerson::getRecno)
                .map(String::valueOf)
                .map(Link.apply(Personalressurs.class, "ansattnummer"))
                .ifPresent(tilskuddFartoy::addSaksansvarlig);

        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "soknadsnummer", caseResult.getExternalId().getValue().getId().getValue()));

        return tilskuddFartoy;
    }


    public List<TilskuddFartoyResource> toFintResourceList(List<CaseResult> caseResults) throws GetDocumentException, IllegalCaseNumberFormat {
        List<TilskuddFartoyResource> result = new ArrayList<>(caseResults.size());
        for (CaseResult caseResult : caseResults) {
            result.add(toFintResource(caseResult));
        }
        return result;
    }

    public CreateCaseParameter convertToCreateCase(TilskuddFartoyResource tilskuddFartoy) {
        CreateCaseParameter createCaseParameter = objectFactory.createCreateCaseParameter();

        tilskuddFartoyDefaults.applyDefaultsToCreateCase(tilskuddFartoy, createCaseParameter);

        createCaseParameter.setTitle(objectFactory.createCaseParameterBaseTitle(TitleParser.getTitleString(tilskuddFartoy)));
        createCaseParameter.setExternalId(P360Utils.getExternalIdParameter(tilskuddFartoy.getSoknadsnummer()));

        applyParameterFromLink(
                tilskuddFartoy.getAdministrativEnhet(),
                s -> objectFactory.createCaseParameterBaseResponsibleEnterpriseRecno(Integer.valueOf(s)),
                createCaseParameter::setResponsibleEnterpriseRecno
        );

        applyParameterFromLink(
                tilskuddFartoy.getArkivdel(),
                objectFactory::createCaseParameterBaseSubArchive,
                createCaseParameter::setSubArchive
        );

        applyParameterFromLink(
                tilskuddFartoy.getSaksstatus(),
                objectFactory::createCaseParameterBaseStatus,
                createCaseParameter::setStatus
        );

        if (tilskuddFartoy.getSkjerming() != null) {
            applyParameterFromLink(
                    tilskuddFartoy.getSkjerming().getTilgangsrestriksjon(),
                    objectFactory::createCaseParameterBaseAccessCode,
                    createCaseParameter::setAccessCode);

            applyParameterFromLink(
                    tilskuddFartoy.getSkjerming().getSkjermingshjemmel(),
                    objectFactory::createCaseParameterBaseParagraph,
                    createCaseParameter::setParagraph);

            // TODO createCaseParameter.setAccessGroup();
        }

        // TODO Missing parameters
        createCaseParameter.setCategory(objectFactory.createCaseParameterBaseCategory("recno:99999"));
        //createCaseParameter.setRemarks();
        //createCaseParameter.setStartDate();
        //createCaseParameter.setUnofficialTitle();

        ArrayOfCaseContactParameter arrayOfCaseContactParameter = objectFactory.createArrayOfCaseContactParameter();
        tilskuddFartoy
                .getPart()
                .stream()
                .map(this::createCaseContactParameter)
                .forEach(arrayOfCaseContactParameter.getCaseContactParameter()::add);
        createCaseParameter.setContacts(objectFactory.createCaseParameterBaseContacts(arrayOfCaseContactParameter));

        ArrayOfRemark arrayOfRemark = objectFactory.createArrayOfRemark();
        if (tilskuddFartoy.getMerknad() != null) {
            tilskuddFartoy
                    .getMerknad()
                    .stream()
                    .map(this::createCaseRemarkParameter)
                    .forEach(arrayOfRemark.getRemark()::add);
        }
        createCaseParameter.setRemarks(objectFactory.createCaseParameterBaseRemarks(arrayOfRemark));

        // TODO Responsible person
        /*
        createCaseParameter.setResponsiblePersonIdNumber(
                objectFactory.createCaseParameterBaseResponsiblePersonIdNumber(
                        tilskuddFartoy.getSaksansvarlig().get(0).getHref()
                )
        );
        */

        return createCaseParameter;
    }

    private Remark createCaseRemarkParameter(MerknadResource merknadResource) {
        Remark remark = objectFactory.createRemark();
        remark.setContent(objectFactory.createRemarkContent(merknadResource.getMerknadstekst()));

        merknadResource
                .getMerknadstype()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(objectFactory::createRemarkRemarkType)
                .findFirst()
                .ifPresent(remark::setRemarkType);

        return remark;
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

    public CreateDocumentParameter convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        CreateDocumentParameter createDocumentParameter = journalpostFactory.toP360(journalpostResource, caseNumber);
        return createDocumentParameter;
    }
}
