package aop;

import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import exception.LoginException;
import logic.User;

@Component
@Aspect
public class AdminLoginAspect {
	@Around("execution(* controller.AdminController.*(..)) && args(..,session)")
	public Object adminCheck(ProceedingJoinPoint joinPoint, 
			HttpSession session)throws Throwable{
		User loginUser = (User)session.getAttribute("loginUser");
		System.out.println(loginUser);
		if(loginUser == null) {
			throw new LoginException("[adminCheck]로그인하삼", "../user/login");
		}
		if(!loginUser.getUserid().equals("admin")) {
			throw new LoginException("[adminCheck]관리자만가능", "../"
					+ "user/mypage?userid="+loginUser.getUserid());
		}
		return joinPoint.proceed();
	}
	
}
