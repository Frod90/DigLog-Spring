package api.store.diglog.common.auth;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.common.exception.ErrorCode;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.login.KakaoResponse;
import api.store.diglog.model.dto.login.OAuth2Response;
import api.store.diglog.model.dto.member.MemberInfoResponse;
import api.store.diglog.model.entity.Member;
import api.store.diglog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;

        switch (registrationId) {
            case "kakao" -> oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
            default -> {
                return null;
            }
        }

        Optional<Member> optionalMember = memberRepository.findByEmail(oAuth2Response.getEmail());
        Member member;
        if (optionalMember.isEmpty()) {
            String username = oAuth2Response.getName();
            if (memberRepository.countByUsername(username) > 0) {
                username = username + "_" + UUID.randomUUID().toString().substring(0, 4);
            }

            // 회원 정보가 없는 경우 저장
            member = Member.builder()
                    .email(oAuth2Response.getEmail())
                    .username(username)
                    .password(UUID.randomUUID().toString())
                    .roles(new HashSet<>(Set.of(Role.ROLE_USER)))
                    .platform(oAuth2Response.getPlatform())
                    .build();

            memberRepository.save(member);
        } else {

            member = optionalMember.get();

            if (!member.getPlatform().equals(oAuth2Response.getPlatform())) {
                // 플랫폼이 같지 않은 경우 중복 회원가입 방지
                throw new CustomException(ErrorCode.SIGNUP_PLATFORM_DUPLICATED);
            }

            memberRepository.save(member);
        }

        MemberInfoResponse memberInfoResponse = MemberInfoResponse.builder()
                .email(member.getEmail())
                .username(member.getUsername())
                .roles(member.getRoles().stream().map(Role::getRole).collect(Collectors.toSet()))
                .build();

        return new CustomOAuth2User(memberInfoResponse);
    }
}
