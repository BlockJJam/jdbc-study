package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
class MemberServiceV3_1Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String EX = "ex";

    private MemberRepositoryV3 memberRepository;
    private MemberServiceV3_1 memberService;

    @BeforeEach
    void before(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV3(dataSource);
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource); // 데이터 접근 기술을 바꾸려면 이 부분에서 JpaTransactionManager처럼 다른 트랜잭션 매니저를 주입해주면 된다
        memberService = new MemberServiceV3_1(transactionManager, memberRepository);
    }

    @AfterEach
    void after() throws SQLException{
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(EX);
    }


    @DisplayName("정상 이체")
    @Test
    void accountTransfer() throws SQLException {
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        log.info("Start TX");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        // no DataSource set 에러가 뜨면, 이는 위의 Transaction Manager를 생성할 때, dataSource를 파라미터에 넣어주지 않으니까, 커넥션 자체를 못 만드는 상황에 뜨는 에러였다.
        log.info("end TX");

        // then
        Member fromMember = memberRepository.findById(memberA.getMemberId());
        Member toMember = memberRepository.findById(memberB.getMemberId());
        assertThat(fromMember.getMoney()).isEqualTo(8000);
        assertThat(toMember.getMoney()).isEqualTo(12000);
    }

    @DisplayName("이체 중 예외 발생")
    @Test
    void accountTransfer_ex() throws SQLException{
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        assertThatThrownBy(()-> memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000))
            .isInstanceOf(IllegalStateException.class);

        // then
        Member fromMember = memberRepository.findById(memberA.getMemberId());
        Member toMember = memberRepository.findById(memberB.getMemberId());
        assertThat(fromMember.getMoney()).isEqualTo(10000);
        assertThat(toMember.getMoney()).isEqualTo(10000);
    }
}