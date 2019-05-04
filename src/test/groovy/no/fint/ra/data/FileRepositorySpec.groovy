package no.fint.ra.data

import no.fint.model.resource.administrasjon.arkiv.DokumentfilResource
import no.fint.ra.AdapterProps
import no.fint.ra.data.utilities.FintUtils
import org.codehaus.jackson.map.ObjectMapper
import org.springframework.http.MediaType
import spock.lang.Specification

class FileRepositorySpec extends Specification {

    private FileRepository fileRepository
    private DokumentfilResource dokumentfilResource

    void setup() {
        fileRepository = new FileRepository(objectMapper: new ObjectMapper(), props: new AdapterProps(p360User: "RA/TEST", p360Password: "topsecret", fileCacheDirectory: new File("build").toPath()))
        fileRepository.addFile(new File("build/1.json").toPath())
        dokumentfilResource = new DokumentfilResource()
        dokumentfilResource.setData("Dette er en test")
        dokumentfilResource.setFormat("plain/text")
        dokumentfilResource.setSystemId(FintUtils.createIdentifikator("1"))

    }

    def "Save file to local file system"() {

        when:
        fileRepository.saveFile(dokumentfilResource)
        def file = new File('build/1.json')
        def exists = file.exists()

        then:
        exists

    }

    def "Read file from local file system"() {
        given:
        fileRepository.saveFile(dokumentfilResource)

        when:
        def file = fileRepository.readFile("1")

        then:
        file.data == "Dette er en test"
        file.systemId.identifikatorverdi == "1"
    }

    def "Get content type from format (file extention)"() {

        when:
        def contentType = fileRepository.getContentType("PDF")

        then:
        contentType == MediaType.APPLICATION_PDF_VALUE


    }

    def "Add file to repository"() {

        given:
        def file = new File("2.json")

        when:
        fileRepository.addFile(file.toPath())

        then:
        fileRepository.filenames.size() == 2

    }

    def "Check if file exists in repository"() {

        when:
        def exists = fileRepository.fileExists("1")

        then:
        exists
    }

    def "Scan for files in cache"() {
        when:
        fileRepository.scan()

        then:
        fileRepository.filenames.size() > 1
    }
}
