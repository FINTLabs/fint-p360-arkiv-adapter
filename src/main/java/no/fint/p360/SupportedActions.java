package no.fint.p360;

import no.fint.adapter.AbstractSupportedActions;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SupportedActions extends AbstractSupportedActions {

    @PostConstruct
    public void addSupportedActions() {

        add(KulturminnevernActions.UPDATE_TILSKUDDFARTOY);
        add(KulturminnevernActions.GET_TILSKUDDFARTOY);

        add(ArkivActions.GET_DOKUMENTFIL);
        add(ArkivActions.UPDATE_DOKUMENTFIL);
        add(ArkivActions.GET_ALL_SAKSSTATUS);
        add(ArkivActions.GET_ALL_DOKUMENTSTATUS);
        add(ArkivActions.GET_ALL_DOKUMENTTYPE);
        add(ArkivActions.GET_ALL_TILKNYTTETREGISTRERINGSOM);
        add(ArkivActions.GET_ALL_KORRESPONDANSEPARTTYPE);
        add(ArkivActions.GET_ALL_JOURNALSTATUS);
        add(ArkivActions.GET_ALL_JOURNALPOSTTYPE);
        add(ArkivActions.GET_ALL_PARTROLLE);
        add(ArkivActions.GET_ALL_MERKNADSTYPE);
        add(ArkivActions.GET_SAK);
        add(ArkivActions.GET_KORRESPONDANSEPART);
        add(ArkivActions.UPDATE_KORRESPONDANSEPART);
        add(ArkivActions.GET_PART);
    }

}
