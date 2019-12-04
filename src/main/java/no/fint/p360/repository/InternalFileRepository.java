package no.fint.p360.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.DokumentfilResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Slf4j
public class InternalFileRepository {

    private AtomicLong identifier =
            new AtomicLong(Long
                    .parseLong(DateTimeFormatter
                            .ofPattern("yyyyDDDHHmm'000'")
                            .format(LocalDateTime
                                    .now())));

    @Value("${fint.internal-files.directory}")
    private Path rootDirectory;

    @Autowired
    private ObjectMapper objectMapper;

    public void putFile(DokumentfilResource resource) throws IOException {
        String systemId = String.format("I_%d", identifier.incrementAndGet());
        resource.setSystemId(FintUtils.createIdentifikator(systemId));
        Path path = rootDirectory.resolve(systemId + ".json");
        objectMapper.writeValue(Files.newOutputStream(path), resource);
        log.info("File saved as {}", path);
    }

    public DokumentfilResource getFile(String recNo) throws IOException {
        Path path = rootDirectory.resolve(recNo + ".json");
        return objectMapper.readValue(Files.newInputStream(path), DokumentfilResource.class);
    }

    public DokumentfilResource silentGetFile(String recNo) {
        try {
            return getFile(recNo);
        } catch (IOException e) {
            return null;
        }
    }

    public boolean health() {
        return Files.isDirectory(rootDirectory);
    }

    public boolean exists(String recNo) {
        return Files.isReadable(rootDirectory.resolve(recNo + ".json"));
    }
}
