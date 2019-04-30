package no.fint.ra;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Getter
@Component
public class AdapterProps {

    @Value("${fint.ra.p360.user}")
    private String p360User;

    @Value("${fint.ra.p360.password}")
    private String p360Password;

    @Value("${fint.file-repository.cache-directory:file-cache}")
    private Path fileCacheDirectory;

    @Value("${fint.file-repository.cache-spec:expireAfterAccess=5m,expireAfterWrite=7m}")
    private String cacheSpec;

}