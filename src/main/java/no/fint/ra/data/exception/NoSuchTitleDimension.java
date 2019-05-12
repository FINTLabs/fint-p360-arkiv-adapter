package no.fint.ra.data.exception;

public class NoSuchTitleDimension extends RuntimeException {
    public NoSuchTitleDimension() {
    }

    public NoSuchTitleDimension(String message) {
        super(message);
    }
}
