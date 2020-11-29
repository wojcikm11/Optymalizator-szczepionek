package exception;

public class ParticipantNotInFileException extends RuntimeException {
    public ParticipantNotInFileException(String message) {
        super(message);
    }
}
