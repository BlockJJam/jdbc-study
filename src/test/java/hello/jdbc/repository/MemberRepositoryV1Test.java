package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV1Test {
    MemberRepositoryV1 repository;

    @BeforeEach
    void beforeEach(){
        // 기본 DriverManager - 항상 새로운 커넥션을 획득
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // conection pooling
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new MemberRepositoryV1(dataSource);
    }



    @Test
    void crud() throws SQLException { // 2번 실행하면 예외발생!, memberId가 primary key로 잡혀있기 때문
        // save
        Member member1 = new Member("memberV5", 10000);
        repository.save(member1);

        // findById
        Member findMember = repository.findById(member1.getMemberId());
        log.info("findMember= {}", findMember);
        assertThat(findMember).isEqualTo(member1);

        // update: money: 10000 -> 20000
        repository.update(member1.getMemberId(), 20000);
        Member updatedMember = repository.findById(member1.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        // delete
        repository.delete(member1.getMemberId());
        assertThatThrownBy(() -> repository.findById(member1.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);

        try{
            Thread.sleep(2000);
        }catch (Exception e){
            log.error("sleep error");
        }
    }
}