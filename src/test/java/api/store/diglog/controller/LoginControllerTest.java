package api.store.diglog.controller;

import api.store.diglog.common.auth.JWTUtil;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.login.LoginRequest;
import api.store.diglog.model.dto.login.LogoutRequest;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Refresh;
import api.store.diglog.repository.MemberRepository;
import api.store.diglog.repository.RefreshRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RefreshRepository refreshRepository;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach() {
        memberRepository.save(getMember());
    }

    @AfterEach
    void afterEach() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인 성공 시 refreshToken을 저장하고, accessToken, refreshToken, 멤버 정보를 return 한다.")
    void login() throws Exception {
        // given
        LoginRequest dto = LoginRequest.builder()
                .email("test@example.com")
                .password("qwer1234")
                .build();

        // when
        MvcResult result = setMockMvc("post", "/api/member/login", dto);
        MockHttpServletResponse response = result.getResponse();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString());

        // then
        assertThat(refreshRepository.countByRefreshToken(response.getCookie("refreshToken").getValue())).isEqualTo(1);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Authorization")).startsWith("Bearer ");
        assertThat(response.getCookie("refreshToken").getValue()).isNotNull();
        assertThat(data.get("email").asText()).isEqualTo("test@example.com");
        assertThat(data.get("username").asText()).isEqualTo("username");
        assertThat(data.get("roles").get(0).asText()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("이메일이 일치하지 않는 경우 에러를 반환한다.")
    void login2() throws Exception {
        // given
        LoginRequest dto = LoginRequest.builder()
                .email("test2222@example.com")
                .password("qwer1234")
                .build();

        // when
        MvcResult result = setMockMvc("post", "/api/member/login", dto);
        MockHttpServletResponse response = result.getResponse();
        String content = result.getResponse().getContentAsString();

        // then
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않는 경우 에러를 반환한다.")
    void login3() throws Exception {
        // given
        LoginRequest dto = LoginRequest.builder()
                .email("test@example.com")
                .password("qwer5678")
                .build();

        // when
        MvcResult result = setMockMvc("post", "/api/member/login", dto);
        MockHttpServletResponse response = result.getResponse();
        String content = result.getResponse().getContentAsString();

        // then
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("로그아웃 시 유효기간이 0인 refreshToken을 return 한다.")
    void logout() throws Exception {
        // given
        LogoutRequest dto = LogoutRequest.builder()
                .email("test@example.com")
                .build();

        // when
        MvcResult result = setMockMvc("post", "/api/member/logout", dto);
        MockHttpServletResponse response = result.getResponse();
        String content = result.getResponse().getContentAsString();

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getCookie("refreshToken").getValue()).isNotNull();
    }

    private MvcResult setMockMvc(String httpMethod, String api, Object dto) throws Exception {
        if (httpMethod.equals("post")) {
            return mockMvc.perform(post(api)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andReturn();
        } else if (httpMethod.equals("get")) {
            return mockMvc.perform(get(api)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andReturn();
        }
        return null;
    }

    @Test
    @DisplayName("토큰을 포함하여 accessToken을 요청하면 accessToken과 멤버 정보를 return 한다.")
    void refresh() throws Exception {
        // given
        Cookie refreshTokenCookie = jwtUtil.generateRefreshCookie(getMember());
        refreshRepository.deleteAll();
        refreshRepository.save(Refresh.builder()
                .email(getMember().getEmail())
                .jwt(refreshTokenCookie.getValue())
                .build());

        // when
        MvcResult result = mockMvc.perform(get("/api/member/refresh")
                        .cookie(refreshTokenCookie))
                .andReturn();
        MockHttpServletResponse response = result.getResponse();
        String content = result.getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(content);

        // then
        assertThat(response.getHeader("Authorization")).startsWith("Bearer ");
        assertThat(data.get("status").asInt()).isEqualTo(200);
        assertThat(data.get("email").asText()).isEqualTo("test@example.com");
        assertThat(data.get("username").asText()).isEqualTo("username");
        assertThat(data.get("roles").get(0).asText()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("토큰이 없는 경우 멤버 정보가 없는 Response를 반환한다.")
    void refresh2() throws Exception {
        // given
        // when
        MvcResult result = mockMvc.perform(get("/api/member/refresh"))
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString());

        // then
        assertThat(data.get("status").asInt()).isEqualTo(401);
    }

    private Member getMember() {
        return Member.builder()
                .email("test@example.com")
                .username("username")
                .password(passwordEncoder.encode("qwer1234"))
                .roles(Set.of(Role.ROLE_USER))
                .build();
    }
}