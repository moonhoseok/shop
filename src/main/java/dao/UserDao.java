package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import logic.User;

@Repository
public class UserDao {
	private NamedParameterJdbcTemplate template;
	private RowMapper<User> mapper = new BeanPropertyRowMapper<User>(User.class);
	private Map<String, Object> param = new HashMap<>();
	@Autowired
	public void setDataSource(DataSource datasSource) {
		template = new NamedParameterJdbcTemplate(datasSource);
	}
	
	public void insert(User user) {
		//param : user 객체의 프로퍼티를 이용하여 db에 값 등록
		SqlParameterSource param = new BeanPropertySqlParameterSource(user);
		String sql ="insert into useraccount (userid,username,password,"
				+ " birthday, phoneno, postcode, address, email) values"
				+ " (:userid,:username,:password,:birthday,:phoneno,:postcode,"
				+ " :address, :email)";
		template.update(sql, param);
		
	}

	public User selectOne(String userid) {
		param.clear();
		param.put("userid", userid);
		return template.queryForObject
		("select * from useraccount where userid=:userid", param, mapper);
	}

	public void update(User user) {
		SqlParameterSource param = new BeanPropertySqlParameterSource(user);
		String sql ="update useraccount set username=:username, birthday=:birthday,"
				+ " phoneno=:phoneno,postcode=:postcode, address=:address,"
				+ " email=:email where userid=:userid";
		template.update(sql, param);
	}

	public void delete(String userid) {
		param.clear();
		param.put("userid", userid);
		template.update("delete from useraccount where userid=:userid",param);
	}

	public void pwupdate(String userid, String chgpass) {
		param.clear();
		param.put("userid", userid);
		param.put("chgpass", chgpass);
		template.update("update useraccount set password=:chgpass where userid=:userid",param);
		
	}
	public List<User> userList() {
		// mapper : User의프로퍼티랑 useraccount 컬럼 이름 1:1 매칭
		return template.query
				("select * from useraccount", param,mapper);
													 					
	}
	// select * from useraccount where userid in ('admin','test1'..)
	public List<User> list(String[] idchks) {
		/*
		 * List<User> list = new ArrayList<>(); for(int i=0; idchks.length > i; i++) {
		 * param.clear(); param.put("userid",idchks[i]); list.addAll(template.query
		 * ("select * from useraccount where userid =:userid", param,mapper)); } return
		 * list;
		 */
		StringBuilder ids = new StringBuilder();
		for(int i = 0; i<idchks.length; i++) {
			ids.append("'").append(idchks[i]).append((i==idchks.length-1)?"'":"',");	
			/*
			 * if(idchks.length-1 > i) { ids.append(","); }
			 */
			 
		}
		System.out.println(ids);
		return template.query
			("select * from useraccount where userid in("+ids+")", param, mapper);
	}

	public String search(User user) {
		String col = "userid";
		if(user.getUserid() != null) col ="password";
		String sql = "select " + col + " from useraccount"
				+" where email=:email and phoneno=:phoneno";
		if(user.getUserid() != null) {
			sql += " and userid=:userid";
		}
		/*
		 * BeanPropertySqlParameterSource(user) : user 객체의 프로퍼티로 파라미터로 설정 
		 * 										:email : user.getEmail()
		 * 										:phoneno : user.getPhoneno()
		 * 					
		 * 			String.class : select 구문의 결과의 자료형				
		 * 
		 * mapper = new BeanPropertyRowMapper<User>(User.class);
		 * 			mapper : select 구문의 실행 결과값
		 * 	1. User 객체 생성
		 *  2. user.setUserid(userid 컬럼값)  
		 * 	
		 */
		SqlParameterSource param=
				new BeanPropertySqlParameterSource(user);
		return template.queryForObject(sql, param, String.class);
	}


}
