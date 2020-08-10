package no.fint.p360.data.p360;

import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.arkiv.p360.support.GetCodeTableRowsResult;
import no.fint.p360.data.exception.CodeTableNotFound;

import java.util.stream.Stream;

public interface P360SupportService {
    GetCodeTableRowsResult getCodeTable(String table) throws CodeTableNotFound;

    Stream<CodeTableRowResult> getCodeTableRowResultStream(String table);

    boolean ping();

    String getSIFVersion();
}
