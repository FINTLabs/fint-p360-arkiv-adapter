package no.fint.p360.data.p360.soap;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.file.*;
import no.fint.p360.AdapterProps;
import no.fint.p360.data.exception.FileNotFound;
import no.fint.p360.data.p360.P360FileService;
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
public class P360FileServiceSOAP extends P360AbstractSOAPService implements P360FileService {


    private static final QName SERVICE_NAME = new QName("http://software-innovation.com/SI.Data", "FileService");

    private IFileService fileServicePort;
    private ObjectFactory objectFactory;

    @Autowired
    private AdapterProps props;

    @Value("${fint.p360.wsdl-location:./src/main/resources/wsdl}/FileService.wsdl")
    private String wsdlLocation;

    public P360FileServiceSOAP() {
        super("http://software-innovation.com/SI.Data", "FileService");
    }

    @PostConstruct
    private void init() throws MalformedURLException {
        URL wsdlLocationUrl = P360Utils.getURL(wsdlLocation);
        log.info("WSDL location: {}", wsdlLocationUrl);
        fileServicePort = new FileService(wsdlLocationUrl, SERVICE_NAME).getBasicHttpBindingIFileService();
        objectFactory = new ObjectFactory();

    }

    @Override public FileResult getFileByRecNo(String recNo) {
        log.info("Retrieving {} ...", recNo);
        GetFileWithMetadataQuery getFileWithMetadataQuery = objectFactory.createGetFileWithMetadataQuery();
        getFileWithMetadataQuery.setRecno(objectFactory.createGetFileWithMetadataQueryRecno(Integer.parseInt(recNo)));
        getFileWithMetadataQuery.setIncludeFileData(objectFactory.createGetFileWithMetadataQueryIncludeFileData(true));
        getFileWithMetadataQuery.setADContextUser(objectFactory.createFileParameterBaseADContextUser(props.getP360User()));
        GetFileWithMetadataResult fileWithMetadata = fileServicePort.getFileWithMetadata((getFileWithMetadataQuery));

        if (fileWithMetadata.isSuccessful()) {
            log.info("Retrieving {} successfully", recNo);
            return fileWithMetadata.getFile().getValue();
        }

        log.info("Retrieving {} failed: {}", recNo, fileWithMetadata.getErrorDetails().getValue());
        throw new FileNotFound(fileWithMetadata.getErrorMessage().getValue());
    }


    @Override public boolean ping() {

        try {
            fileServicePort.ping();
        } catch (WebServiceException e) {
            return false;
        }

        return true;
    }
}
