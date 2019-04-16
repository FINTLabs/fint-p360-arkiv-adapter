package no.fint.ra;

import no.fint.adapter.AbstractSupportedActions;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.administrasjon.personal.PersonalActions;
import no.fint.model.felles.FellesActions;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SupportedActions extends AbstractSupportedActions {

    @PostConstruct
    public void addSupportedActions() {

        add(KulturminnevernActions.UPDATE_TILSKUDDFARTOY);
        add(KulturminnevernActions.GET_TILSKUDDFARTOY);
        //add(ArkivActions.GET_JOURNALPOST);
    }

}
