package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class UncheckedTest {
    @Test
    void uncheckedCatch(){
        Service service = new Service();
        service.callCatch();
        assertThatThrownBy(() -> service.callCatch())
                .isInstanceOf(MyUncheckedException.class);

    }

    @Test
    void uncheckedThrow(){
        Service service = new Service();
        assertThatThrownBy(()-> service.callThrow())
                .isInstanceOf(MyUncheckedException.class);
    }

    /**
     * RuntimeException을 상속받은 예외는 언체크 예외가 된다.
     */
    static class MyUncheckedException extends RuntimeException{
        public MyUncheckedException(String message) {
            super(message);
        }
    }


    static class Repository{
        public void call(){ // throws RuntimeException 선언을 안해도 된다(물론 해도상관은 없다)
            throw new MyUncheckedException("ex");
        }
    }

    /**
     * Unchecked 예외는 예외를 잡거나, 던지지 않아도 된다.
     * 예외를 잡지 않으면 자동으로 밖으로 던진다.
     */
    static class Service{
        Repository repository = new Repository();

        // 필요한 경우에 예외를 잡아서 처리해도 된다.
        public void callCatch(){
            try{
                repository.call();
            }catch (MyUncheckedException e){
                log.info("예외 처리, message={}", e.getMessage(), e);
            }

            repository.call();
        }

        // 예외를 잡지 않아도 상위로 던져진다. (체크 예외와는 다르게 throws 선언이 필요없다)
        public void callThrow(){
            repository.call();
        }
    }


}
