package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import logic.Board;


@Repository
public class BoardDao {
	private NamedParameterJdbcTemplate template;
	private Map<String, Object> param = new HashMap<>();
	// 조회된 컬럼명과 Board클래스의 프로퍼티가 같은 값을 Board객체 생성
	private RowMapper<Board> mapper = 
				new BeanPropertyRowMapper<>(Board.class);
	private String select =" select num,writer,pass,"
			+ "title,content,file1 fileurl,"
			+ "regdate,readcnt,grp, grplevel, "
			+ "grpstep,boardid from board";
	@Autowired // spring-db.xml에서 설정한 datasource 객체 주입
	public void setDataSource(DataSource dataSource) {
		// datasource : db연결객체.
		//NamedParameterJdbcTemplate : spring 프레임워크의 jdbc템플릿
		template = new NamedParameterJdbcTemplate(dataSource);
	}
	public int maxNum() {
		return template.queryForObject
				("select ifnull(max(num),0) from board", param, Integer.class );
	}
	public void insert(@Valid Board board) {
		SqlParameterSource param = new BeanPropertySqlParameterSource(board);
		String sql = "insert into board"
				+ " (num, writer,pass,title,content,file1,regdate,"
				+ " readcnt, grp,grplevel,grpstep,boardid)"
				+ " values (:num, :writer,:pass,:title,:content,:fileurl,now(),"
				+ "	:readcnt, :grp,:grplevel,:grpstep,:boardid)";
		template.update(sql, param);
		
	}
	public int count(String boardid,String column, String find) {
		String sql = "select count(*) from board where boardid=:boardid";
		param.clear();
		param.put("boardid", boardid);
		if(column != null && find != null) {
			sql += " and "+ column +" like :find";
			param.put("find", "%"+find+"%");
		}
		return template.queryForObject(sql, param, Integer.class);
	}
	
	
	public List<Board> list(Integer pageNum, int limit, String boardid
			,String column, String find) {
		param.clear();
		String sql = select;
		sql += " where boardid =:boardid ";
		if(column != null && find != null) {
			sql += " and " + column + " like :find";
			param.put("find", "%"+find+"%");
		}
		sql += " order by grp desc, grpstep asc "
			+ " limit :startrow, :limit";
		param.put("startrow", (pageNum-1)*limit); //1페이지 :0 , 2페이지 :10
		param.put("limit", limit);
		param.put("boardid", boardid);
		return template.query(sql, param, mapper);
	}
	public Board selectOne(Integer num) {
		param.clear();
		String sql = select;
		sql += " where num=:num";
		param.put("num",num);
		return template.queryForObject(sql, param,mapper);
	}
	public void addReadcnt(Integer num) {
		param.clear();
		param.put("num", num);
		String sql = "update board set readcnt=readcnt+1 where num =:num";
		template.update(sql, param);
	}
	
	public void updateGrpStep(Board board) {
		String sql = "update board set grpstep=grpstep+1"
				+ " where grp =:grp and grpstep >:grpstep";
		param.clear();
		param.put("grp", board.getGrp());
		param.put("grpstep", board.getGrpstep());
		template.update(sql, param);
	}
	public void update(Board board) {
		String sql = "update board set writer=:writer, title=:title,"
				+ " content=:content, file1=:fileurl where num=:num";
		SqlParameterSource param = 
				new BeanPropertySqlParameterSource(board);
		template.update(sql, param);
	}
	public void delete(Integer num) {
		template.update("delete from board where num="+num, param);
	}
	
}
