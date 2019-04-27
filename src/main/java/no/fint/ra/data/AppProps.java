package no.fint.ra.data;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppProps {

    @Value("${fint.ra.p360.user}")
    private String p360User;

    @Value("${fint.ra.p360.password}")
    private String p360Password;

    @Value("${fint.file-cache-directory:data}")
    private String fileCacheDirectory;

}