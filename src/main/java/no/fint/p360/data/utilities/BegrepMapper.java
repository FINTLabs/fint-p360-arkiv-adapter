package no.fint.p360.data.utilities;

import no.fint.arkiv.p360.support.CodeTableRowResult;
import no.fint.model.felles.basisklasser.Begrep;

import java.util.function.Function;
import java.util.function.Supplier;

public class BegrepMapper {
    public static <T extends Begrep> Function<CodeTableRowResult,T> mapValue(Supplier<T> constructor) {
        return value -> {
            T result = constructor.get();
            result.setSystemId(FintUtils.createIdentifikator(value.getRecno().toString()));
            result.setKode(value.getCode());
            result.setNavn(value.getDescription());
            return result;
        };
    }
}
