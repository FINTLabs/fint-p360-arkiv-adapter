package no.fint.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaultsService;
import no.fint.arkiv.CaseProperties;
import no.fint.arkiv.p360.caze.CreateCaseParameter;
import no.fint.model.resource.arkiv.noark.SaksmappeResource;
import no.fint.p360.data.utilities.Constants;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class P360CaseDefaultsService extends CaseDefaultsService {

    public CreateCaseParameter applyDefaultsToCreateCaseParameter(CaseProperties properties, SaksmappeResource saksmappeResource, CreateCaseParameter createCaseParameter) {

        applyDefaultsForCreation(properties, saksmappeResource);
        // TODO createCaseParameter.setKeywords(Arrays.asList(properties.getNoekkelord()));
        createCaseParameter.setFiledOnPaper(false);
        createCaseParameter.setCaseType(Constants.CASE_TYPE_NOARK);

        return createCaseParameter;
    }


}
