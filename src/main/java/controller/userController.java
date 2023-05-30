package controller;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import exception.LoginException;
import logic.Item;
import logic.Sale;
import logic.ShopService;
import logic.User;

@Controller
@RequestMapping("user")
public class userController {
	@Autowired
	private ShopService service;
	// idsearch , pwsearch
	@GetMapping("*") // * : 설정되지 않은 모든요청시 호출되는 메서드
	public ModelAndView join () {
		ModelAndView mav = new ModelAndView();
		mav.addObject(new User());
		return mav;
	}
	@PostMapping("join")
	public ModelAndView userAdd(@Valid User user, BindingResult bresult) {
		ModelAndView mav = new ModelAndView();
		if(bresult.hasErrors()) {
			mav.getModel().putAll(bresult.getModel());
			// reject : global error에 추가
			bresult.reject("error.input.user");
			bresult.reject("error.input.check");
			return mav;
		}
		// 정상입력값 : 회원가입하기 => db의 useraccount 테이블에 저장
		try {
			service.userinsert(user);
			mav.addObject("user",user);
		} catch (DataIntegrityViolationException e) {
			// DataIntegrityViolationException : db등록시 Key값 중복 오류 발생
			// Duplicate entry 'adimin' for key 'PRIMARY'
			e.printStackTrace();
			bresult.reject("error.duplicate.user"); //global 오류 등록
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		mav.setViewName("redirect:login");
		return mav;
	}
	
	@PostMapping("login")
	public ModelAndView login
	(@Valid User user, BindingResult bresult,HttpSession session) {
		ModelAndView mav = new ModelAndView();
		if(bresult.hasErrors()) {
			mav.getModel().putAll(bresult.getModel());
			bresult.reject("error.login.input");
			return mav;
		}
		//1. userid 맞는 User를 db에서 조회하기
		User dbUser = null;
		try {
			dbUser = service.selectUserOne(user.getUserid());
		} catch(EmptyResultDataAccessException e) {//조회된 데이터가 없는 경우 발생 예외
			e.printStackTrace();
			bresult.reject("error.login.id"); //아이디를 확인하세요 
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		//2. 비밀번호 검증  
		//   일치 : session.setAttribute("loginUser",dbUser) => 로그인 정보
		//   불일치 : 비밀번호를 확인하세요. 출력 (error.login.password)
		//3. mypage로 페이지 이동 => 404 오류 발생 (임시)
		if(user.getPassword().equals(dbUser.getPassword())) { //정상 로그인
			session.setAttribute("loginUser", dbUser);
			mav.setViewName("redirect:mypage?userid="+user.getUserid());
		}else {  
			bresult.reject("error.login.password");
			mav.getModel().putAll(bresult.getModel());
		}		
		return mav;
	}
	/*
	 * AOP:설정하기:
	 *  UserLoginAspect 클래스의 userIdCheck 메서드 구현하기
	 * 1. pointcut : UserController 클래스의 idCheck로 시작하는 메서드이고,
	 * 				마지막 매개변수의 userid,session인 경우
	 * 2. 로그인 여부 검증
	 * 	- 로그인이 안된경우 로그인 후 거래하세요. 메세지출력 후 login페이지 호출
	 * 3. admin이 아니면서, 로그인 아이디와 파라미터 userid값이 다른 경우
	 * 	- 본인만 거래 가능합니다. 메세지 출력 후 item/list 페이지 호출
	 */
	@RequestMapping("mypage")
	public ModelAndView idCheckMypage(String userid,HttpSession session) {
		ModelAndView mav = new ModelAndView();
		User user = service.selectUserOne(userid);
		List<Sale> salelist = service.salelist(userid);
		mav.addObject("user", user); //회원정보데이터
		mav.addObject("salelist", salelist);  //주문목록
		return mav;
	} 
	//로그아웃
	//login페이지로이동
	@RequestMapping("logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:login";
	}
	
	//회원정보수정
	//로그인상태,본인정보만 조회검증=> AOP클래스(UserLoginAspect.userIdCheck())검증
	
	@GetMapping({"update","delete"}) 
	public ModelAndView idCheckUpdate(String userid,HttpSession session) {
		ModelAndView mav = new ModelAndView();
		User user = service.selectUserOne(userid);
		mav.addObject("user", user); //회원정보데이터
		return mav;
	}
	/*
	 * 회원탈퇴
	 * 1. 파라미터정보저장.(userid, password)
	 *  - 관리자인 경우 탈퇴불가
	 * 2. 비밀번호 검증 => 로그인된 비밀번호 검증
	 *   본인탈퇴 : 본인 비밀번호
	 *   관리자가 타인 탈퇴 : 관리자 비밀번호
	 * 3. 비밀번호 불일치
	 * 	 메세지 출력 후 delete로 페이지이동
	 * 4. 비밀번호 일치
	 * 	 db에서 해당 사용자 정보 삭제하기
	 * 	- 본인탈퇴 : 로그아웃, login페이지 이동
	 * 	- 관리자탈퇴 : admin/list페이지 이동 => 404 오류 발생
	 */
	@PostMapping("delete")
	public String idCheckdelete
			(String password,String userid,HttpSession session) {
		//관리자 탈퇴불가
		if(userid.equals("admin")) {
			throw new LoginException
			("관리자 탈퇴불가", "mypage?userid="+userid);
		}
		// 비밀번호검증 : 로그인된 정보
		User loginUser = (User)session.getAttribute("loginUser"); //로그인 정보
		// password : 입력된 비밀번호 (파라미터값)
		// loginUser.getPassword() : 로그인된 사용자의 비밀번호
		if(!password.equals(loginUser.getPassword())) {
			throw new LoginException
			("비밀번호를 확인하세요", "delete?userid="+userid);
		}
		// 비밀번호 일치 : 고객정보 제거
		try {
			service.userdelete(userid);
		// 주문정보 존재
		}catch (DataIntegrityViolationException e){
			throw new LoginException
			("주문정보가 존재합니다 관리자 연락 요망", "mypage?userid="+userid);
		}catch (Exception e) {
			e.printStackTrace();
			throw new LoginException
			("탈퇴시 오류발생", "delete?userid="+userid);
		}
		// 탈퇴성공 : 회원정보를 제거 한 상태 
		if(loginUser.getUserid().equals("admin")) { // 관리자가 강제 탈퇴
			return "redirect:../admin/list";
		}else { 									//본인 탈퇴
			session.invalidate();
			return "redirect:login";
		}
	}
	/*
	public ModelAndView delete(String userid,HttpSession session, String pw) {
		ModelAndView mav = new ModelAndView();
		User user = service.selectUserOne(userid); // 입력정보
		User loginUser = (User)session.getAttribute("loginUser"); //로그인 정보
		if(loginUser.getUserid().equals("admin")&&
				user.getUserid().equals("admin")) {
			throw new LoginException
				("관리자 탈퇴불가", "mypage?userid="+loginUser.getUserid());
		//본인확인
		}else if(loginUser.getUserid().equals(user.getUserid())) {
			//비밀번호 확인
			if(!loginUser.getPassword().equals(pw)) {
				throw new LoginException
				("비밀번호 불일치", "mypage?userid="+loginUser.getUserid());
			}
			service.userdelete(userid);
			session.invalidate();
			mav.setViewName("redirect:login");
		//관리자가 타인 탈퇴
		}else {
			User ad = service.selectUserOne(loginUser.getUserid());
			if(loginUser.getPassword().equals(pw)) {
				service.userdelete(userid);
				mav.setViewName("redirect:admin/list");
			}else {
				throw new LoginException
				("비밀번호 불일치", "mypage?userid="+loginUser.getUserid());
			}
		}
		mav.addObject("user", user); //회원정보데이터
		return mav;
	}
	*/
	@PostMapping("update")
	public ModelAndView idCheckUpdate(@Valid User user, BindingResult bresult,
			String userid,HttpSession session) {
		ModelAndView mav = new ModelAndView();
		// 입력값 검증
		if(bresult.hasErrors()) {
			mav.getModel().putAll(bresult.getModel());
			// reject : global error에 추가
			bresult.reject("error.update.user");
			return mav;
		}
		// 비밀번호 검증
		User loginUser = (User)session.getAttribute("loginUser");
		//비밀번호 불일치
		//user.getPassword() : 회원이 입력한 비밀번호
		if(!user.getPassword().equals(loginUser.getPassword())) {
			bresult.reject("error.login.password");
			return mav;
		}
		// 비밀번호 일치
		try {
			service.userupdate(user);
			if(loginUser.getUserid().equals(user.getUserid())) {
				session.setAttribute("loginUser",user);
			}
			mav.setViewName("redirect:mypage?userid="+user.getUserid());
		} catch (Exception e) {
			e.printStackTrace();
			throw new LoginException("고객정보 수정 실패","update?userid="+user.getUserid());
		}
		return mav;
	}
	/* UserLoginAspect.loginCheck() : UserController.loginCheck*(..) 인 메서드
	 * 1. 로그인검증 => AOP 클래스.
	 * 2. 파라미터값을 저장 (매개변수)
	 * 3. 현재비밀번호와 입력된 비밀번호 검증
	 *   불일치 : 오류메세지출력 . password로 이동
	 * 4. 일치 : db 수정
	 * 5. 성공 : 로그인 정보 변경. mypage로 이동
	 * 	  실패 : 오류메세지 출력. password로 이동
	 * 		 
	 */
	@PostMapping("password")
	public String loginCheckpassword(String password,
					String chgpass,HttpSession session) {
		// 비밀번호 검증
		User loginUser = (User)session.getAttribute("loginUser");
		// password : 현재 비밀번호
		//loginUser.getPassword() : 로그인된 비밀번호 
		if(!password.equals(loginUser.getPassword())) {
			throw new LoginException
			("비밀번호를 확인하세요", "password?userid="+loginUser.getUserid());
		}
		try {
			service.userpassword(loginUser.getUserid(), chgpass);
			User user = service.selectUserOne(loginUser.getUserid());
			//session.setAttribute("loginUser",user); // 로그인정보에 바뀐 정보로 수정
			loginUser.setPassword(chgpass);
			return "redirect:mypage?userid="+loginUser.getUserid();
		} catch (Exception e) {
			e.printStackTrace();
			throw new LoginException
				("비밀번호 수정 실패","mypage?userid="+loginUser.getUserid());
		}
	}
	//"{url}search" : ${url} 지정되지 않음. *search 인 요청시 호출되는 메서드
	@PostMapping("{url}search")
	public ModelAndView search(User user, BindingResult bresult, @PathVariable String url) {
		//@PathVariable : {url}의 이름을 매개변수로 전달
		// 요청 : idsearch : url <= "id"
		// 요청 : pwsearch : url <= "id"
		ModelAndView mav = new ModelAndView();
		String code = "error.userid.search";
		String title = "아이디";
		if(url.equals("pw")) { // 비밀번호 검증인 경우
			title = "비밀번호";
			code ="error.password.search";
			if(user.getUserid()==null || user.getUserid().trim().equals("")) {
				// BindingResult.reject() : global error
				// => jsp의 <spring:hasBindError...> 부분에 오류 출력
				// BindingResult.rejectValue() :
				// => jsp의 <form:error path="" ...> 부분에 오류 출력
				bresult.rejectValue("userid","error.required"); // error.required.userid 오류코드
			}
		}
		if(user.getEmail()==null || user.getEmail().trim().equals("")) {
			bresult.rejectValue("email","error.required");
			// error.required.email 오류코드
		}
		if(user.getPhoneno()==null || user.getPhoneno().trim().equals("")) {
			bresult.rejectValue("phoneno","error.required");
			// error.required.phoneno 오류코드
		}
		if(bresult.hasErrors()) {
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		//입력값검증 정상완료 후
		if(user.getUserid() != null && user.getUserid().trim().equals("")) {
			user.setUserid(null);
		}
		/*										result
		 * user.getUserid() == null : 아이디찾기 => 아이디
		 * user.getUserid() != null : 비밀번호찾기 => 비밀번호
		 */
		String result = null;
		try {
			result = service.getSearch(user);
		} catch (EmptyResultDataAccessException e) {
			bresult.reject(code);
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		System.out.println("result = " +result);
		mav.addObject("result",result);
		mav.addObject("title",title);
		mav.setViewName("search");
		return mav;
	}
	
	
	
	
	
	
	
	
	
}
