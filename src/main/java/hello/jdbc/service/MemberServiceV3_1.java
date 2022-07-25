package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {
    // 문제는 dataSource를 그대로 가져다 쓰는 것 -> PlatfromTransactionManager로 변경해보자
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try{
            // 비즈니스 로직
            bizLogic(fromId, toId, money);
            // 커밋, 롤
            transactionManager.commit(status); // 성공 시 커밋
        }catch(Exception e){
            transactionManager.rollback(status); // 실패 시 커밋
            throw new IllegalStateException(e);
        }
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        // 트랜잭션 시작
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void release(Connection con) {
        if(con != null){
            try{
                con.setAutoCommit(true); // 기본 설정인 auto commit을 자동으로 변경해줘야 한다. 커넥션 풀 고려
                con.close();
            }catch(Exception e){
                log.error("connection error", e);
            }
        }
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("계좌 이체 중에 예외 발생");
        }
    }
}
