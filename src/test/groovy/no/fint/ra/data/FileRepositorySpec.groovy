package no.fint.ra.data

import com.google.common.cache.LoadingCache
import no.fint.model.resource.administrasjon.arkiv.DokumentfilResource
import no.fint.ra.AdapterProps
import no.fint.ra.data.utilities.FintUtils
import org.codehaus.jackson.map.ObjectMapper
import org.springframework.http.MediaType
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileRepositorySpec extends Specification {

    private FileRepository fileRepository
    private DokumentfilResource dokumentfilResource
    private LoadingCache<String, Path> cache

    void setup() {
        cache = Mock()
        fileRepository = new FileRepository(files: cache, objectMapper: new ObjectMapper(), props: new AdapterProps(p360User: "RA/TEST", p360Password: "topsecret", fileCacheDirectory: new File("build").toPath()))
        dokumentfilResource = new DokumentfilResource()
        dokumentfilResource.setData("Dette er en test")
        dokumentfilResource.setFormat("text/plain")
        dokumentfilResource.setSystemId(FintUtils.createIdentifikator("1"))

    }

    def "Save file to local file system"() {

        when:
        fileRepository.putFile(dokumentfilResource)
        def path = Paths.get('build/1.json')

        then:
        Files.exists(path)

    }

    def "Read file from local file system"() {
        given:
        fileRepository.putFile(dokumentfilResource)

        when:
        def file = fileRepository.getFile("1")

        then:
        file.data == "Dette er en test"
        file.systemId.identifikatorverdi == "1"
        1 * cache.get('1') >> Paths.get('build/1.json')
    }

    def "Get content type from format (file extension)"() {

        when:
        def contentType = fileRepository.getContentType("PDF")

        then:
        contentType == MediaType.APPLICATION_PDF_VALUE


    }

    def "Scan for files in cache"() {
        when:
        fileRepository.scan()

        then:
        1 * cache.put('1', _ as Path)
    }
}
