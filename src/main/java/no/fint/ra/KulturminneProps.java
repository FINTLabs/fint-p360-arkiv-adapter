package no.fint.ra;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class KulturminneProps {

    @Value("${fint.kulturminne.tilskudd-fartoy.arkivdel}")
    private Integer responsibleUnit;

    @Value("${fint.kulturminne.tilskudd-fartoy.sub-archive}")
    private String subArchive;

    @Value("${fint.kulturminne.tilskudd-fartoy.keywords}")
    private String[] keywords;

}
