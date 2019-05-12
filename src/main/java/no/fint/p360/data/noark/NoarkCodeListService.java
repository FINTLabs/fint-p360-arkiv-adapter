package no.fint.p360.data.noark;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.TilknyttetRegistreringSomResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class NoarkCodeListService {

    @Getter
    private List<TilknyttetRegistreringSomResource> tilknyttetRegistreringSom;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("code-lists/tilknyttetregistringsom.json");
        tilknyttetRegistreringSom = objectMapper.readValue(resource.getInputStream(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TilknyttetRegistreringSomResource.class));

    }

}
