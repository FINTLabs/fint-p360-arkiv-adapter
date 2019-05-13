package no.fint.p360.data.noark.journalpost;


import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.model.resource.administrasjon.arkiv.SaksmappeResource;
import no.fint.p360.data.p360.P360DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class JournalpostService {

    @Autowired
    private P360DocumentService documentService;

    @Autowired
    private JournalpostFactory factory;

    public void createJournalPost(SaksmappeResource sak) {

        List<JournalpostResource> journalpostResources = sak.getJournalpost();

        journalpostResources.forEach(journalpostResource ->
                documentService.createDocument(
                        factory.toP360(journalpostResource,
                                sak.getMappeId().getIdentifikatorverdi())
                )
        );
    }

    public JournalpostResource getJournalPost(String systemId) {
        return factory.toFintResource(documentService.getDocumentBySystemId(systemId));
    }



}
