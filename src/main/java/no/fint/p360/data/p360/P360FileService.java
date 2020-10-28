package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.file.*;
import no.fint.p360.AdapterProps;
import no.fint.p360.data.exception.FileNotFound;
import no.fint.p360.data.utilities.P360Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
public class P360FileService extends P360AbstractService {


    private static final QName SERVICE_NAME = new QName("http://software-innovation.com/SI.Data", "FileService");

    private IFileService fileServicePort;
    private ObjectFactory objectFactory;

    @Autowired
    private AdapterProps props;

    @Value("${fint.p360.wsdl-location:./src/main/resources/wsdl}/FileService.wsdl")
    private String wsdlLocation;

    public P360FileService() {
        super("http://software-innovation.com/SI.Data", "FileService");
    }

    @PostConstruct
    private void init() throws MalformedURLException {
        URL wsdlLocationUrl = P360Utils.getURL(wsdlLocation);
        log.info("WSDL location: {}", wsdlLocationUrl);
        fileServicePort = new FileService(wsdlLocationUrl, SERVICE_NAME).getBasicHttpBindingIFileService();
        objectFactory = new ObjectFactory();

    }

    public FileResult getFileByRecNo(String recNo) {
        log.info("Retrieving {} ...", recNo);
        GetFileWithMetadataQuery getFileWithMetadataQuery = objectFactory.createGetFileWithMetadataQuery();
        getFileWithMetadataQuery.setRecno(Integer.parseInt(recNo));
        getFileWithMetadataQuery.setIncludeFileData(true);
        getFileWithMetadataQuery.setADContextUser(props.getP360User());
        GetFileWithMetadataResult fileWithMetadata = fileServicePort.getFileWithMetadata((getFileWithMetadataQuery));

        if (fileWithMetadata.isSuccessful()) {
            log.info("Retrieving {} successfully", recNo);
            return fileWithMetadata.getFile();
        }

        log.info("Retrieving {} failed: {}", recNo, fileWithMetadata.getErrorDetails());
        throw new FileNotFound(fileWithMetadata.getErrorMessage());
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
