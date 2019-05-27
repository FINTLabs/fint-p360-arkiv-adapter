package no.fint.p360;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.fint.p360.data.p360.P360SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private P360SupportService supportService;

    @GetMapping(value = "codelist", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getCodelist(@RequestParam String id) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(supportService.getCodeTable(id));
    }
}
