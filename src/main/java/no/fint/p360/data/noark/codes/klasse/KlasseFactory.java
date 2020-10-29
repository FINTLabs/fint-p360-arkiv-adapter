package no.fint.p360.data.noark.codes.klasse;


import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.ArchiveCodeResult;
import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.arkiv.noark.Klassifikasjonssystem;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KlasseFactory {

    public KlasseResource toFintResource(CodeTableRowResult codeTableRow) {
        KlasseResource klasseResource = new KlasseResource();
        klasseResource.setKlasseId(codeTableRow.getCode());
        klasseResource.setTittel(codeTableRow.getDescription());
        return klasseResource;
    }

    public KlasseResource toFintResource(ArchiveCodeResult archiveCodeResult) {
        KlasseResource klasseResource = new KlasseResource();
        klasseResource.setKlasseId(archiveCodeResult.getArchiveCode());
        klasseResource.addKlassifikasjonssystem(Link.with(Klassifikasjonssystem.class, "systemid", archiveCodeResult.getArchiveType()));
        return klasseResource;
    }
}
