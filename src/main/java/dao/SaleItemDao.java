package dao;

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

import logic.SaleItem;

@Repository
public class SaleItemDao {
	private NamedParameterJdbcTemplate template;
	private Map<String, Object> param = new HashMap<>();
	private RowMapper<SaleItem> mapper = 
			new BeanPropertyRowMapper<>(SaleItem.class);
	@Autowired // spring-db.xml에서 설정한 datasource 객체 주입
	public void setDataSource(DataSource dataSource) {
		// datasource : db연결객체.
		//NamedParameterJdbcTemplate : spring 프레임워크의 jdbc템플릿
		template = new NamedParameterJdbcTemplate(dataSource);
	}
	
	public void insert(SaleItem saleItem) {
		String sql = "insert into saleitem (saleid,seq,itemid,quantity)"
				+ " values(:saleid, :seq, :itemid, :quantity)";
		SqlParameterSource param = 
				new BeanPropertySqlParameterSource(saleItem);
		template.update(sql, param);
	}

	public List<SaleItem> list(int saleid) {
		param.clear();
		param.put("saleid", saleid);
		String sql ="select * from saleitem where saleid = :saleid";
		return template.query(sql,param,mapper);
	}

}
