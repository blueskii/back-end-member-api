package com.mycompany.backendservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.crypto.factory.PasswordEncoderFactories;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.backendservice.dto.Login;
import com.mycompany.backendservice.dto.Member;
import com.mycompany.backendservice.security.AppUserDetails;
import com.mycompany.backendservice.security.AppUserDetailsService;
import com.mycompany.backendservice.security.JwtProvider;
import com.mycompany.backendservice.service.MemberService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/member")
public class MemberController {
	@Autowired
	private JwtProvider jwtProvider;
	
	@Autowired
	private AppUserDetailsService userDetailsService;
	
	@Autowired
	private MemberService memberService;
	
	@PostMapping("/join")
	public Member join(@Valid @RequestBody Member member) {
		//비밀번호 암호화
		PasswordEncoder passwordEncoder  = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		member.setMpassword(passwordEncoder.encode(member.getMpassword()));
		//아이디 활성화 설정
		member.setMenabled(true);
		//권한 설정
		member.setMrole("ROLE_USER");
		//회원 가입 처리
		memberService.join(member);
		return member;
	}

	@PostMapping("/login")
	public Map<String, String> login(@Valid Login login) {
		//사용자 상세 정보 얻기
		AppUserDetails userDetails = (AppUserDetails) userDetailsService.loadUserByUsername(login.getMid());
		//비밀번호 체크
		PasswordEncoder passwordEncoder  = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		boolean checkResult = passwordEncoder.matches(login.getMpassword(), userDetails.getMember().getMpassword());
		//스프링 시큐리티 인증 처리
		if(checkResult == true) {
			Authentication authentication = 
					new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		//응답 생성
		Map<String, String> map = new HashMap<>();
		if(checkResult) {
			//AccessToken을 생성
			String accessToken = jwtProvider.createAccessToken(login.getMid(), userDetails.getMember().getMrole());
			log.info("ROLE: " + userDetails.getMember().getMrole());
			//JSON 응답 구성
			map.put("result", "success");
			map.put("mid", login.getMid());
			map.put("accessToken", accessToken);
		} else {
			map.put("result", "fail");
		}
		return map;
	}	
}










