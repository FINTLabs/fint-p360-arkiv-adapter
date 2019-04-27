package no.fint.ra.data;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.HeaderConstants;
import no.fint.ra.data.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/file", produces = {MediaType.APPLICATION_PDF_VALUE})
public class FileController {

    @Autowired
    private FileRepository fileRepository;

    @GetMapping("/{id}")
    public ResponseEntity<FileSystemResource> getAvatarByFileName(@PathVariable String id,
                                                                  @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
                                                                  @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) {
        log.info("{}, orgId {}, client {}", id, orgId, client);

        String file = fileRepository.getFile(id);

        if (file != null) {
            return ResponseEntity.ok(new FileSystemResource(file));

        }
        throw new EntityNotFoundException(String.format("File (%s) could not be found", id));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleEntityNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

}