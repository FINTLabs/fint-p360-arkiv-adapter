package no.fint.ra.data.noark;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.administrasjon.arkiv.TilknyttetRegistreringSom;
import no.fint.model.resource.administrasjon.arkiv.TilknyttetRegistreringSomResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

// FIXME: 2019-05-08 Add caching and mapping @asgeir
@Slf4j
@Service
public class NoarkCodeListService {

    private List<TilknyttetRegistreringSomResource> tilknyttetRegistreringSomList;


    @PostConstruct
    public void init() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            File file = new ClassPathResource("code-lists/tilknyttetregistringsom.json").getFile();
            String json = new String(Files.readAllBytes(file.toPath()));
            tilknyttetRegistreringSomList = objectMapper.readValue(json, new TypeReference<List<TilknyttetRegistreringSomResource>>() {
            });
        } catch (IOException e) {
            log.error("Unable to read code list file: ", e);
        }

    }

    public List<TilknyttetRegistreringSomResource> getTilknyttetRegistreringSom() {

        return tilknyttetRegistreringSomList;

    }
}
