package api.store.diglog.controller;

import api.store.diglog.model.dto.tag.TagResponse;
import api.store.diglog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/{username}")
    public ResponseEntity<List<TagResponse>> getMemberTags(@PathVariable("username") String username) {
        List<TagResponse> tagResponses = tagService.getMemberTags(username);

        return ResponseEntity.ok().body(tagResponses);
    }
}
