package com.mycompany.backendservice;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.formParameters;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

//Lombok의 @Slf4j는 Gralde Test에는 출력되지 않고, JUnit Test에만 출력됨(사용하지 말것) 
@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class BackEndServiceApplicationTests {
	private MockMvc mockMvc;
	
	@BeforeEach
	void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(documentationConfiguration(restDocumentation)) 
			.apply(SecurityMockMvcConfigurers.springSecurity()) // Spring Security 통합
			.build();
	}
	
	@Test
	void contextLoads() {
	}
	
	@Test
	@Transactional //테스트 메서드 실행 시 트랜잭션이 시작되고 종료 시 롤백됨
	void memberJoin() throws Exception {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("mid", "user1");
		jsonObject.put("mname", "사용자1");
		jsonObject.put("mpassword", "Ab123");
		jsonObject.put("memail", "user1@naver.com");
		String json = jsonObject.toString();
		
		mockMvc.perform(post("/member/join")
			.content(json)
			.contentType(MediaType.APPLICATION_JSON)
		)
		.andExpect(status().isOk())
		.andDo(document("member/join", 
			preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
			requestFields(
				fieldWithPath("mid").description("회원 아이디"),
				fieldWithPath("mname").description("회원 이름"),
				fieldWithPath("mpassword").description("회원 비밀번호"),
				fieldWithPath("memail").description("회원 이메일")
			),
			responseFields(
				fieldWithPath("mid").description("회원 아이디"),
	            fieldWithPath("mname").description("회원 이름"),
	            fieldWithPath("mpassword").description("회원 비밀 번호(암호화됨)"),
	            fieldWithPath("menabled").description("회원 활성화 여부"),
	            fieldWithPath("mrole").description("회원에 부여된 롤"),
	            fieldWithPath("memail").description("회원의 이메일")
	        ))); 		
	}
	
	@Test
	@Transactional
	void memberLogin() throws Exception {
		mockMvc.perform(post("/member/login")
			.param("mid", "user")
			.param("mpassword", "12345")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		)
		.andExpectAll(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		.andExpectAll(
			jsonPath("$.result").value("success"),
			jsonPath("$.mid").value("user"),
			jsonPath("$.accessToken").isNotEmpty())
		.andDo(document("member/login", 
			preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
			formParameters(
				parameterWithName("mid").description("회원 아이디"),
				parameterWithName("mpassword").description("회원 비밀번호")
			),
			responseFields(
				fieldWithPath("result").description("처리 결과"),
	            fieldWithPath("mid").description("회원 아이디"),
	            fieldWithPath("accessToken").description("액세스 토큰")
		    ))); 
	}		
}






