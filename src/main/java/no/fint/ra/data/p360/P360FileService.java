package no.fint.ra.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.file.*;
import no.fint.ra.data.AppProps;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class P360FileService extends P360AbstractService {


    private static final QName SERVICE_NAME = new QName("http://software-innovation.com/SI.Data", "FileService");

    private IFileService fileServicePort;
    private ObjectFactory objectFactory;

    @Autowired
    private AppProps props;

    public P360FileService() {
        super("http://software-innovation.com/SI.Data", "FileService");
    }

    @PostConstruct
    private void init() {

        fileServicePort = new FileService(FileService.WSDL_LOCATION, SERVICE_NAME).getBasicHttpBindingIFileService();
        //super.addAuthentication(fileServicePort);

        objectFactory = new ObjectFactory();

    }

    public File saveFile(byte[] file, String recNo, String format) {
        File fileDestination = new File(String.format("%s/%s.%s", props.getFileCacheDirectory(), recNo, format));
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileDestination);
            fileOutputStream.write(file);
            fileOutputStream.close();
        } catch (IOException e) {
            log.error("Failed to save file", e);
            return null;
        }
        return fileDestination;

    }

    public File getAndSaveFileByRecNo(String recNo) {
        GetFileWithMetadataQuery getFileWithMetadataQuery = objectFactory.createGetFileWithMetadataQuery();
        getFileWithMetadataQuery.setRecno(objectFactory.createGetFileWithMetadataQueryRecno(Integer.parseInt(recNo)));
        getFileWithMetadataQuery.setIncludeFileData(objectFactory.createGetFileWithMetadataQueryIncludeFileData(true));
        getFileWithMetadataQuery.setADContextUser(objectFactory.createFileParameterBaseADContextUser(props.getP360User()));
        GetFileWithMetadataResult fileWithMetadata = fileServicePort.getFileWithMetadata((getFileWithMetadataQuery));

        if (fileWithMetadata.isSuccessful()) {
            FileResult fileResult = fileWithMetadata.getFile().getValue();
            return saveFile(getBytesFromBase64(fileResult.getBase64Data().getValue()), recNo, fileResult.getFormat().getValue());
        }

        return null;

    }

    private byte[] getBytesFromBase64(String s) {
        Base64 base64 = new Base64();
        return base64.decode(s);
    }


    public boolean ping() {

        try {
            fileServicePort.ping();
        } catch (WebServiceException e) {
            return false;
        }

        return true;
    }
}
