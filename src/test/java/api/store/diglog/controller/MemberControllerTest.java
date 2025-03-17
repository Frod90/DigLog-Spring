package api.store.diglog.controller;

import api.store.diglog.common.auth.JWTUtil;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.member.MemberUsernameRequest;
import api.store.diglog.model.entity.Member;
import api.store.diglog.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTUtil jwtUtil;

    @BeforeEach
    void beforeEach() {
        memberRepository.save(defaultMember("test@example.com"));
    }

    @AfterEach
    void afterEach() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("username 변경 요청을 성공한다.")
    void updateUsername() throws Exception {
        // given
        MemberUsernameRequest dto = MemberUsernameRequest.builder()
                .username("newUsername")
                .build();

        // when
        MvcResult result = mockMvc.perform(post("/api/member/username")
                        .header("Authorization", getAuthorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();
        MockHttpServletResponse response = result.getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(memberRepository.findByEmail("test@example.com").get().getUsername()).isEqualTo("newUsername");
    }

    @Test
    @DisplayName("accessToken을 통해 사용자 정보를 받아온다.")
    void getProfile() throws Exception {
        // given
        // when
        MvcResult result = mockMvc.perform(get("/api/member/profile")
                        .header("Authorization", getAuthorization()))
                .andReturn();
        MockHttpServletResponse response = result.getResponse();
        JsonNode data = objectMapper.readTree(response.getContentAsString());

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(data.get("email").asText()).isEqualTo("test@example.com");
        assertThat(data.get("username").asText()).isEqualTo("test");
    }

    @Test
    @DisplayName("로그인 상태가 아닌 경우 401을 반환한다.")
    void getProfile2() throws Exception {
        // given
        // when
        MvcResult result = mockMvc.perform(get("/api/member/profile"))
                .andReturn();
        MockHttpServletResponse response = result.getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("username으로 다른 사용자의 profile 정보를 받아온다.")
    void getProfileByUsername() throws Exception {
        // given
        String username = "test";

        // when
        MvcResult result = mockMvc.perform(get("/api/member/profile/" + username))
                .andReturn();
        MockHttpServletResponse response = result.getResponse();
        JsonNode data = objectMapper.readTree(response.getContentAsString());

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(data.get("username").asText()).isEqualTo("test");
    }

    @Test
    @DisplayName("username으로 다른 사용자 profile을 검색한다.")
    void searchProfileByUsername() throws Exception {
        // given
        String username = "username=eS"; // test
        String page = "page=0";
        String size = "size=10";
        String parameter = "?" + username + "&" + page + "&" + size;

        // when
        MvcResult result = mockMvc.perform(get("/api/member/profile/search" + parameter))
                .andReturn();
        MockHttpServletResponse response = result.getResponse();
        JsonNode data = objectMapper.readTree(response.getContentAsString());

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(data.get("content").get(0).get("username").asText()).isEqualTo("test");
    }

    private Member defaultMember(String email) {
        return Member.builder()
                .email(email)
                .password(passwordEncoder.encode("qwer1234"))
                .roles(Set.of(Role.ROLE_USER))
                .username(email.split("@")[0])
                .isDeleted(false)
                .build();
    }

    private String getAuthorization() {
        return "Bearer " + jwtUtil.generateAccessToken(defaultMember("test@example.com"));
    }
}