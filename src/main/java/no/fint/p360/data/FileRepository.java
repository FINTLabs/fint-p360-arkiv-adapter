package no.fint.p360.data;

import com.google.common.cache.*;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.file.FileResult;
import no.fint.model.resource.administrasjon.arkiv.DokumentfilResource;
import no.fint.p360.AdapterProps;
import no.fint.p360.data.exception.FileNotFound;
import no.fint.p360.data.p360.P360FileService;
import no.fint.p360.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@Slf4j
@Repository
public class FileRepository extends CacheLoader<String, Path> implements RemovalListener<String, Path> {

    @Autowired
    private AdapterProps props;

    @Autowired
    private P360FileService fileService;

    private ObjectMapper objectMapper;

    private LoadingCache<String, Path> files;

    @PostConstruct
    public void init() throws IOException {
        files = CacheBuilder.from(props.getCacheSpec()).removalListener(this).build(this);
        if (!Files.exists(props.getFileCacheDirectory())) {
            Files.createDirectories(props.getFileCacheDirectory());
        }
        if (!Files.isDirectory(props.getFileCacheDirectory())) {
            throw new IllegalArgumentException("Not a directory: " + props.getFileCacheDirectory());
        }

        objectMapper = new ObjectMapper();
    }

    @Scheduled(initialDelay = 10000, fixedDelayString = "${fint.file-repository.scan-interval:1500000}")
    public void scan() {
        try {
            log.info("Start scanning cache directory for files.");
            Files.walk(props.getFileCacheDirectory()).filter(Files::isRegularFile).map(Path::toAbsolutePath).forEach(this::addFile); 
            log.info("Finished scanning cache directory. {} file(s) in repository", files.size());
        } catch (IOException e) {
            log.error("During scan", e);
        }
    }

    @Scheduled(initialDelay = 20000, fixedDelayString = "${fint.file-repository.clean-interval:150000}")
    public void cleanUp() {
        files.cleanUp();
    }

    private void addFile(Path path) {
        String id = getId(path);
        files.put(id, path);
    }

    private String getId(Path path) {
        return StringUtils.removeEndIgnoreCase(path.getFileName().toString(), ".json");
    }

    public DokumentfilResource getFile(String recNo) throws FileNotFound {
        try {
            Path path = files.get(recNo);
            return readFile(path);
        } catch (ExecutionException | IOException e) {
            throw new FileNotFound(e.getMessage());
        }

    }

    public void putFile(DokumentfilResource resource) throws IOException {
        Path path = saveFile(resource);
        files.put(getId(path), path);
        log.info("File saved as {}", path);
    }

    private String getContentType(String format) {
        Tika tika = new Tika();
        return tika.detect(String.format("fil.%s", format));
    }

    private DokumentfilResource readFile(Path path) throws IOException {
        return objectMapper.readValue(Files.newInputStream(path), DokumentfilResource.class);
    }

    private Path saveFile(DokumentfilResource dokumentfilResource) throws IOException {
        Path path = props.getFileCacheDirectory().resolve(dokumentfilResource.getSystemId().getIdentifikatorverdi() + ".json");
        objectMapper.writeValue(Files.newOutputStream(path), dokumentfilResource);
        return path;
    }

    @Override
    public void onRemoval(RemovalNotification<String, Path> removal) {
        if (removal.wasEvicted()) {
            Path path = removal.getValue();
            if (StringUtils.startsWith(path.getFileName().toString(), "I_")) {
                log.info("Not removing {}", path);
                return;
            }
            log.info("Removing {}", path);
            try {
                Files.delete(path);
            } catch (IOException e) {
                log.warn("Unable to delete {}: {}", path, e.getMessage());
            }
        }
    }

    @Override
    public Path load(String recNo) throws Exception {
        log.info("Loading {} ...", recNo);

        if (StringUtils.startsWith(recNo, "I_")) {
            Path path = props.getFileCacheDirectory().resolve(recNo + ".json");
            if (Files.exists(path)) {
                return path;
            }
            throw new FileNotFound(recNo);

        } else {
            FileResult fileResult = fileService.getFileByRecNo(recNo);

            DokumentfilResource dokumentfilResource = new DokumentfilResource();
            dokumentfilResource.setSystemId(FintUtils.createIdentifikator(recNo));
            dokumentfilResource.setData(fileResult.getBase64Data().getValue());
            dokumentfilResource.setFormat(getContentType(fileResult.getFormat().getValue()));

            return saveFile(dokumentfilResource);
        }
    }
}
