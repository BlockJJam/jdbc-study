package hello.jdbc.repository.ex;

public class MyDuplicateKeyException extends MyDbException{
    // 우리가 다루는 DB에서 문제가 생긴 것이다!

    public MyDuplicateKeyException() {
        super();
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
