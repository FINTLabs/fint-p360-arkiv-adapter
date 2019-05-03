package no.fint.ra.data;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.file.FileResult;
import no.fint.model.administrasjon.arkiv.Dokumentfil;
import no.fint.model.resource.administrasjon.arkiv.DokumentfilResource;
import no.fint.ra.data.p360.P360FileService;
import no.fint.ra.data.utilities.FintUtils;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Repository
public class FileRepository {

    @Autowired
    private AppProps props;

    @Autowired
    private P360FileService fileService;

    private ConcurrentMap<String, String> filenames = new ConcurrentSkipListMap<>();

    @PostConstruct
    public void init() {
        if (!Files.isDirectory(props.getFileCacheDirectory()))
            throw new IllegalArgumentException("Not a directory: " + props.getFileCacheDirectory());

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
        //id = id.substring(0, id.lastIndexOf('.'));
        filenames.put(id, filnavn);

    }

    public String getDataUri(String data) {
        String format = "application/pdf";//"application/pdf";
        return String.format("data:%s;base64,%s", format, data);
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

                String dataUri = getDataUri(fileResult.getBase64Data().getValue());
                File file = saveFile(dataUri, recNo, fileResult.getFormat().getValue());
                if (file != null) {
                    addFile(file.toPath());
                    dokumentfilResource.setData(dataUri);
                    return dokumentfilResource;
                }


            } else {
                return null;
            }
        }
        dokumentfilResource.setData(readFile(recNo));
        return dokumentfilResource;
    }

    private String readFile(String recNo) {
        try {
            return new String(Files.readAllBytes(Paths.get(filenames.get(recNo))));
        } catch (IOException e) {
            log.error("Unable to read file from cache", e);
        }

        return null;
    }

    private File saveFile(String data, String recNo, String format) {
        File fileDestination = new File(String.format("%s/%s", props.getFileCacheDirectory().toAbsolutePath(), recNo));
        try {
            PrintWriter printWriter = new PrintWriter(fileDestination);
            printWriter.print(data);
            printWriter.close();
        } catch (IOException e) {
            log.error("Failed to save file", e);
            return null;
        }

        return fileDestination;

    }

    private byte[] getBytesFromBase64(String s) {
        Base64 base64 = new Base64();
        return base64.decode(s);
    }


}
