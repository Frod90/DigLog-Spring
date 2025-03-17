package api.store.diglog.controller;

import api.store.diglog.model.dto.image.ImageRequest;
import api.store.diglog.model.dto.image.ImageUrlResponse;
import api.store.diglog.model.dto.member.MemberProfileInfoResponse;
import api.store.diglog.model.dto.member.MemberProfileResponse;
import api.store.diglog.model.dto.member.MemberProfileSearchRequest;
import api.store.diglog.model.dto.member.MemberUsernameRequest;
import api.store.diglog.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/username")
    public ResponseEntity<Void> updateUsername(@RequestBody MemberUsernameRequest memberUsernameRequest) {
        memberService.updateUsername(memberUsernameRequest);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<MemberProfileResponse> getProfile() {
        MemberProfileResponse memberInfoResponse = memberService.getProfile();

        return ResponseEntity.ok().body(memberInfoResponse);
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<MemberProfileInfoResponse> getProfileByUsername(@PathVariable("username") String username) {
        MemberProfileInfoResponse memberProfileInfoResponse = memberService.getProfileByUsername(username);

        return ResponseEntity.ok().body(memberProfileInfoResponse);
    }

    @GetMapping("/profile/search")
    public ResponseEntity<Page<MemberProfileInfoResponse>> searchByUsername(@ParameterObject @ModelAttribute MemberProfileSearchRequest memberProfileSearchRequest) {
        Page<MemberProfileInfoResponse> memberProfileInfoResponses = memberService.searchProfileByUsername(memberProfileSearchRequest);

        return ResponseEntity.ok().body(memberProfileInfoResponses);
    }

    @PostMapping(
            value = "/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImageUrlResponse> uploadAndSaveImage(ImageRequest imageRequest) {
        ImageUrlResponse imageUrlResponse = memberService.updateProfileImage(imageRequest);

        return ResponseEntity.ok().body(imageUrlResponse);
    }
}
