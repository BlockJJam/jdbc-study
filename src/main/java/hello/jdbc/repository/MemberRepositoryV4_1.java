package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 예외 누수 문제 해결
 * 체크 예외를 런타임 예외로 변경
 * MemberRepository 인터페이스 사용
 * throws SQLException이 제거
 */
@Slf4j
@RequiredArgsConstructor
public class MemberRepositoryV4_1 implements MemberRepository{

    private final DataSource dataSource;

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            // setString : sql에 대한 parameter 바인딩
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        }catch(SQLException e){
            throw new MyDbException(e);
        }finally{
            // 열려있는 connection 관련 객체를 close해준다
            close(con, pstmt, null);
        }
    }

    @Override
    public Member findById(String memberId){
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs  =  pstmt.executeQuery();
            if(rs.next()){ // next? 내부에 커서가 있는데, 한번은 next를 호출해줘 실제 데이터가 있는 곳부터 시작
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else{
                // 만약 데이터가 없으면 이쪽으로 온다
                throw new NoSuchElementException("member not found member_id="+ memberId);

            }

        } catch (SQLException e) {
            throw new MyDbException(e);
        }finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money){
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            int resultSize = pstmt.executeUpdate();
            log.info("resultSize = {} ", resultSize);

        }catch(SQLException e){
            throw new MyDbException(e);
        }finally {
            close(con, pstmt, null);
        }
    }

    public void delete(String memberId){
        String sql = "delete from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs){

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        // 주의! 트랜잭션 동기화를 사용하려면, DataSourceUtils를 사용하자
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면, DataSourceUtils를 사용하자
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection= {}, class = {}", con, con.getClass());
        return con;
    }
}
