package no.fint.ra;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class Props {

    @Value("${fint.kulturminne.tilskudd-fartoy.arkivdel}")
    private String responsibleUnit;

    @Value("${fint.kulturminne.tilskudd-fartoy.sub-archive}")
    private String subArchive;

    @Value("${fint.kulturminne.tilskudd-fartoy.keywords}")
    private String[] keywords;

}
