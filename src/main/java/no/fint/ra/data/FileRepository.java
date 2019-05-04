package no.fint.ra.data;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.file.FileResult;
import no.fint.model.resource.administrasjon.arkiv.DokumentfilResource;
import no.fint.ra.AdapterProps;
import no.fint.ra.data.p360.service.P360FileService;
import no.fint.ra.data.utilities.FintUtils;
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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Repository
public class FileRepository {

    @Autowired
    private AdapterProps props;

    @Autowired
    private P360FileService fileService;

    private ObjectMapper objectMapper;

    private ConcurrentMap<String, String> filenames = new ConcurrentSkipListMap<>();

    @PostConstruct
    public void init() {
        if (!Files.isDirectory(props.getFileCacheDirectory())) {
            throw new IllegalArgumentException("Not a directory: " + props.getFileCacheDirectory());
        }

        objectMapper = new ObjectMapper();


    }

    @Scheduled(initialDelay = 10000, fixedDelayString = "${fint.adapter.avatar.scan-interval:150000}")
    public void scan() {
        try {
            log.info("Start scanning cache directory for files.");
            filenames.clear();
            Files.walk(props.getFileCacheDirectory()).filter(Files::isRegularFile).forEach(this::addFile); //.filter(Objects::nonNull).forEach(repository::add);
            log.info("Finished scanning cache directory. {} file(s) in repository", filenames.size());
        } catch (IOException e) {
            log.error("During scan", e);
        }
    }

    private void addFile(Path path) {
        String filnavn = path.toAbsolutePath().toString();
        String id = path.getFileName().toString();
        id = id.substring(0, id.lastIndexOf('.'));
        filenames.put(id, filnavn);

    }

    private boolean fileExists(String recNo) {
        return filenames.get(recNo) != null;
    }

    public DokumentfilResource getFile(String recNo) {

        DokumentfilResource dokumentfilResource = new DokumentfilResource();
        dokumentfilResource.setSystemId(FintUtils.createIdentifikator(recNo));

        if (!fileExists(recNo)) {
            FileResult fileResult = fileService.getFileByRecNo(recNo);

            if (fileResult != null) {


                dokumentfilResource.setData(fileResult.getBase64Data().getValue());
                dokumentfilResource.setData(fileResult.getBase64Data().getValue());

                dokumentfilResource.setFormat(getContentType(fileResult.getFormat().getValue()));
                File file = saveFile(dokumentfilResource);
                if (file != null) {
                    addFile(file.toPath());
                    return dokumentfilResource;
                }


            } else {
                return null;
            }
        }

        return readFile(recNo);
    }

    private String getContentType(String format) {
        Tika tika = new Tika();
        return tika.detect(String.format("fil.%s", format));
    }

    private DokumentfilResource readFile(String recNo) {
        try {
            return objectMapper.readValue(new File(filenames.get(recNo)), DokumentfilResource.class);
        } catch (IOException e) {
            log.error("Unable to read file from cache", e);
        }

        return null;
    }

    private File saveFile(DokumentfilResource dokumentfilResource) {
        File fileDestination = new File(
                String.format("%s/%s.json", props.getFileCacheDirectory().toAbsolutePath(), dokumentfilResource.getSystemId().getIdentifikatorverdi())
        );
        try {
            objectMapper.writeValue(fileDestination, dokumentfilResource);
        } catch (IOException e) {
            log.error("Failed to save file", e);
            return null;
        }

        return fileDestination;

    }

}
