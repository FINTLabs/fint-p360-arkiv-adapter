package no.fint.p360.data.exception;

public class EnterpriseNotFound extends RuntimeException {
    public EnterpriseNotFound(String errorMessage) {
        super(errorMessage);
    }
}
