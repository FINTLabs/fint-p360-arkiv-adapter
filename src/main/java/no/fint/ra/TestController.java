package no.fint.ra;

import no.fint.ra.data.p360.service.P360SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private P360SupportService supportService;

    @GetMapping("codelist")
    public String getCodelist(@RequestParam String id) {
        return supportService.getCodeTable(id).toString();
    }
}
