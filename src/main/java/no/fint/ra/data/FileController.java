package no.fint.ra.data;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.HeaderConstants;
import no.fint.ra.data.exception.EntityNotFoundException;
import no.fint.ra.data.p360.P360FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/file", produces = {MediaType.APPLICATION_PDF_VALUE})
public class FileController {

    /*
    @Autowired
    AvatarRepository repository;
*/
    @Autowired
    private P360FileService fileService;

    @GetMapping("/{id}")
    public ResponseEntity<FileSystemResource> getAvatarByFileName(@PathVariable String id,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
                                                  @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) {
        log.info("{}, authorization {}, orgId {}, client {}", id, authorization, orgId, client);
        /*
        if (!repository.authorize(id, authorization)) {
            throw new AccessDeniedException("Authorization failure");
        }
        String result = repository.getFilenames().get(id);

        if (result == null)
            throw new EntityNotFoundException(id);
         */
        File file = fileService.getAndSaveFileByRecNo(id);

        if (file != null) {
        return ResponseEntity.ok(new FileSystemResource(file));

        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleEntityNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e);
    }

}