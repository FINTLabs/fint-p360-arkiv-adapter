package no.fint.ra.data;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.HeaderConstants;
import no.fint.model.administrasjon.arkiv.Dokumentfil;
import no.fint.ra.DataUri;
import no.fint.ra.data.exception.EntityNotFoundException;
import no.fint.ra.data.utilities.FintUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/file", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class FileController {

    @Autowired
    private FileRepository fileRepository;

    @GetMapping("/{id}")
    public ResponseEntity getAvatarByFileName(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client,
            @RequestParam(name = "download", required = false) String download,
            HttpServletResponse response) {
        log.info("id {}, download {}, orgId {}, client {}", id, download, orgId, client);

        /*
        String file = fileRepository.getFile(id);
        if (file != null) {
            DataUri dataUri = DataUri.parse(file, Charset.defaultCharset());

            if (downloadFile(download)) {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=" + id + ".pdf");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Cache-Control", "no-cache");
                InputStream is = new ByteArrayInputStream(dataUri.getData());
                try {
                    IOUtils.copy(is, response.getOutputStream());
                    response.flushBuffer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return ResponseEntity.ok().build();
            }
            Dokumentfil dokumentfil = new Dokumentfil();
            dokumentfil.setSystemId(FintUtils.createIdentifikator(id));
            dokumentfil.setData(dataUri.toString());
            return ResponseEntity.ok(dokumentfil);


        }
         */

        throw new EntityNotFoundException(String.format("File (%s) could not be found", id));
    }

    private boolean downloadFile(String download) {
        if (download == null) {
            return false;
        }
        return download.equals("true") || download.equals("");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleEntityNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

}