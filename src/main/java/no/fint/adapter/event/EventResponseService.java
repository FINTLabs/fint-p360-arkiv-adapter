package no.fint.adapter.event;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.fint.adapter.FintAdapterProps;
import no.fint.event.model.Event;
import no.fint.event.model.HeaderConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Handles responses back to the provider status endpoint.
 */
@Slf4j
@Service
public class EventResponseService {

    @Autowired
    private FintAdapterProps props;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Method for posting back the response to the provider.
     *
     * @param event Event to post back
     */
    public void postResponse(Event event) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.put(HeaderConstants.ORG_ID, Lists.newArrayList(event.getOrgId()));
            ResponseEntity<Void> response = restTemplate.exchange(props.getResponseEndpoint(), HttpMethod.POST, new HttpEntity<>(event, headers), Void.class);
            log.info("Provider POST response: {}", response.getStatusCode());
        } catch (RestClientException e) {
            log.warn("Provider POST response error: {}", e.getMessage());
        }
    }
}
