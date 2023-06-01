package logic;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dao.BoardDao;
import dao.ItemDao;
import dao.SaleDao;
import dao.SaleItemDao;
import dao.UserDao;



@Service // @Component + Service(controller 기능과 dao 기능의 중간 역할)
public class ShopService {
	@Autowired // ItemDao 객체주입.
	private ItemDao itemdao;
	@Autowired //  객체주입.
	private UserDao userdao;
	@Autowired //  객체주입.
	private SaleDao saledao;
	@Autowired //  객체주입.
	private SaleItemDao saleItemdao;
	@Autowired //  객체주입.
	private BoardDao boarddao;
	
	public List<Item> itemList(){
		return itemdao.list();
	}

	public Item getItem(Integer id) {

		return itemdao.getItem(id);
	}

	public void itemCreate(Item item, HttpServletRequest request) {
		if(item.getPicture() != null && !item.getPicture().isEmpty()) {
			// 업로드해야하는 파일이 있는 경우  
			String path = request.getServletContext().getRealPath("/")+"img/";
			uploadFileCreate(item.getPicture(),path);
			// 업로드된 파일이름
			item.setPictureUrl(item.getPicture().getOriginalFilename());
		}
		// db에 내용 저장
		int maxid = itemdao.maxId(); // item테이블에 저장된 최대id값
		item.setId(maxid+1); // 최대값보다 하나더 큰값 저장
		itemdao.insert(item); // db에 추가
	}
	//파일 업로드 부분
	public void uploadFileCreate(MultipartFile file, String path) {
		// file : 파일의 내용
		// path : 업로드할 폴더
		String orgFile = file.getOriginalFilename(); // 파일이름
		File f = new File(path);
		if(!f.exists())f.mkdirs();
		try {
			//file에 저장된 내용을 파일로 저장.
			file.transferTo(new File(path+orgFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void itemUpdate(@Valid Item item, HttpServletRequest request) {
		if(item.getPicture() != null && !item.getPicture().isEmpty()) {
			// 업로드해야하는 파일이 있는 경우  
			String path = request.getServletContext().getRealPath("/")+"img/";
			uploadFileCreate(item.getPicture(),path);
			// 업로드된 파일이름
			item.setPictureUrl(item.getPicture().getOriginalFilename());
		}
		itemdao.update(item);
		
	}

	public void itemDelete(Integer id) {
		itemdao.delete(id);
		
	}

	public void userinsert(User user) {
		userdao.insert(user);
		
	}

	public User selectUserOne(String userid) {
		
		return userdao.selectOne(userid);
	}
	/*
	 * 1. 로그인정보, 장바구니정보 => sale, saleitem 테이블의 데이터 저장
	 * 2. 결과는 Sale 객체에 저장
	 * 		-sale테이블 저장 : saleid값 구하기. 최대값 + 1
	 * 		- saleitem 테이블 저장 : Cart 데이터를 이용하여 저장
	 */
	public Sale checkend(User loginUser, Cart cart) {
		int maxsaleid = saledao.getMaxSaleId(); // saleid 최대값 조회
		Sale sale = new Sale();
		sale.setSaleid(maxsaleid+1);			// sale 객체 만들기	
		sale.setUser(loginUser);				// sale 객체 만들기
		sale.setUserid(loginUser.getUserid()); // sale 객체 만들기
		saledao.insert(sale); // sale 테이블에 데이터 추가
		int seq = 0;
		for(ItemSet is : cart.getItemSetList()) {
			SaleItem saleItem = new SaleItem(sale.getSaleid(),++seq,is);
			sale.getItemList().add(saleItem);
			saleItemdao.insert(saleItem); //saleitem 테이블에 데이터 추가
		}
		return sale; // 주문정보, 주문상품정보, 상품정보, 사용자정보
	}
	
	public List<Sale> salelist(String userid) {
		List<Sale> list = saledao.list(userid);//id 사용자가 주문 정복목록 
		for(Sale sa : list) {
			//saleitemlist : 한개의 주문에 해당하는 주문상품 목록
			List<SaleItem> saleitemlist =saleItemdao.list(sa.getSaleid());
			for(SaleItem si : saleitemlist) {
				Item item = itemdao.getItem(si.getItemid()); //상품정보
				si.setItem(item);
			}
			sa.setItemList(saleitemlist);
		}
		return list;
	}

	public void userupdate( User user) {
		userdao.update(user);
	}

	public void userdelete(String userid) {
		userdao.delete(userid);
	}

	public void userpassword(String userid, String chgpass) {
		userdao.pwupdate(userid, chgpass);
		
	}

	public List<User> userList() {
		return userdao.userList();
	}

	public List<User> getUserList(String[] idchks) {
		return userdao.list(idchks);
	}

	public String getSearch(User user) {
		return userdao.search(user);
	
	}

	public void boardWrite(@Valid Board board, HttpServletRequest request) {
		int maxnum = boarddao.maxNum(); //등록된 게시물의 최대 num값을 리턴
		board.setNum(++maxnum);
		board.setGrp(maxnum);
		if(board.getFile1() != null && !board.getFile1().isEmpty()) {
			String path = request.getServletContext().getRealPath("/")
					+"board/file/";
			this.uploadFileCreate(board.getFile1(), path);
			board.setFileurl(board.getFile1().getOriginalFilename());
		}
		boarddao.insert(board);
	}

	public int boardcount(String boardid,String column, String find) {
		return boarddao.count(boardid,column,find);
	}

	public List<Board> boardlist(Integer pageNum, int limit, String boardid
			,String column, String find) {
		return boarddao.list(pageNum,limit,boardid,column,find);
	}

	public Board getBoard(Integer num) {
		return boarddao.selectOne(num);
	}

	public void addReadcnt(Integer num) {
		boarddao.addReadcnt(num);
	}

	/*public Board boardReply(Map<String,String> param) {
		//boarddao.updateGrpstep(param);
		Board board = new Board();
		int maxnum = boarddao.maxNum(); //등록된 게시물의 최대 num값을 리턴
		int grplevel =(Integer.parseInt(param.get("grplevel")));
		int grpstep = (Integer.parseInt(param.get("grpstep")));
		board.setNum(++maxnum);
		board.setBoardid(param.get("boardid"));
		//board.setNum(Integer.parseInt(param.get("num")));
		board.setGrp(Integer.parseInt(param.get("grp")));
		board.setGrplevel(++grplevel);
		board.setGrpstep(++grpstep);
		board.setWriter(param.get("writer"));
		board.setPass(param.get("pass"));
		board.setTitle(param.get("title"));
		board.setContent(param.get("content"));
		boarddao.insert(board);
		return board;
	}*/
	@Transactional // 중간 오류시 롤백 : 트랜잭션처리함. 업무를 원자화한다. ALL or nothing
	public void boardReply(Board board) {
		boarddao.updateGrpStep(board);  //이미 등록된 grpstep값 1씩 증가
		int max = boarddao.maxNum();    //최대 num 조회
		board.setNum(++max);  //원글의 num => 답변글의 num 값으로 변경
		                      //원글의 grp => 답변글의 grp 값을 동일. 설정 필요 없음
                              //원글의 boardid => 답변글의 boardid 값을 동일. 설정 필요 없음
		board.setGrplevel(board.getGrplevel() + 1); //원글의 grplevel => +1 답변글의 grplevel 설정
		board.setGrpstep(board.getGrpstep() + 1);   //원글의 grpstep => +1 답변글의 grpstep 설정
		boarddao.insert(board);
	}

	public void boardUpdate(Board board, HttpServletRequest request) {
		if(board.getFile1() != null && !board.getFile1().isEmpty()) {
			String path = request.getServletContext().getRealPath("/")
					+"board/file/";
			//파일 업로드 : board.getFile1()의 내용을 파일로 생성
			this.uploadFileCreate(board.getFile1(), path); 
			board.setFileurl(board.getFile1().getOriginalFilename());
		}
		boarddao.update(board);
	}

	public void boardDelete(Integer num) {
		boarddao.delete(num);
	}
} 
