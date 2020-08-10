package no.fint.p360.data.p360.rpc;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.AdapterProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Slf4j
public abstract class P360AbstractRPCService {

    private RestTemplate p360Client;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private AdapterProps adapterProps;

    @PostConstruct
    protected void init() {
        p360Client = restTemplateBuilder
                .rootUri(adapterProps.getEndpointBaseUrl())
                .build();
        log.trace("Init.");
    }

    protected <T> T call(String uri, Object args, Class<T> responseType) {
        log.trace("POST {} {}", uri, args);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "authkey " + adapterProps.getP360Password());
        headers.set("clientid", adapterProps.getP360User());
        final ResponseEntity<T> entity = p360Client.exchange(uri, HttpMethod.POST, new HttpEntity<>(args, headers), responseType);
        log.trace("POST response {} {} {}", uri, entity.getStatusCode(), entity.getHeaders());
        return entity.getBody();
    }

    /*
    public boolean getHealth(String url) {
        return p360Client.post().uri(url + "?authkey={p360AuthKey}", p360AuthKey)
                .retrieve()
                .toBodilessEntity()
                .blockOptional()
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .getStatusCode().is2xxSuccessful();
    }

     */
}
