package no.fint.p360.data.p360;

import no.fint.arkiv.p360.file.FileResult;

public interface P360FileService {
    FileResult getFileByRecNo(String recNo);

    boolean ping();
}
