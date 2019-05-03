package no.fint.ra.data


import spock.lang.Specification

class FileRepositorySpec extends Specification {

    def "Save file to local file system"() {

        given:
        def fileService = new FileRepository(props: new AppProps(p360User: "RA/TEST", p360Password: "topsecret", fileCacheDirectory: new File("build").toPath()))
        def testString = "Dette er en test"

        when:
        fileService.saveFile(testString.getBytes(), "123456", "txt")
        def file = new File('build/123456.txt')
        def exists = file.exists()

        then:
        exists

    }
}
