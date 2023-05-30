package aop;

import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import exception.LoginException;
import logic.User;

@Component
@Aspect
public class UserLoginAspect {
	//pointcut
	@Around("execution(* controller.user*.idCheck*(..)) && args(..,userid,session)")
	public Object userIdCheck(ProceedingJoinPoint joinPoint, String userid, 
			HttpSession session)throws Throwable{
		User loginUser = (User)session.getAttribute("loginUser");
		System.out.println(loginUser);
		if(loginUser == null) {
			throw new LoginException("회원만가능", "../user/login");
		}
		if(!loginUser.getUserid().equals("admin")) {
			if(!loginUser.getUserid().equals(userid)) {
				throw new LoginException("본인만가능", "../item/list");
			}
		}
		return joinPoint.proceed();
	}
	@Around
	("execution(* controller.user*.loginCheck*(..)) && args(..,session)")
	public Object loginCheck(ProceedingJoinPoint joinPoint,
			HttpSession session)throws Throwable {
		User loginUser = (User)session.getAttribute("loginUser");
		if(loginUser == null) {
			throw new LoginException("[loginCheck]로그인이 피룡함", "login");
		}
		return joinPoint.proceed();
	}
}
