package api.store.diglog.service;

import api.store.diglog.model.dto.tag.TagResponse;
import api.store.diglog.model.entity.Tag;
import api.store.diglog.model.vo.tag.TagPostVO;
import api.store.diglog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> getMemberTags(String username) {
        return tagRepository.findAllByPostsMemberUsernameOrderByName(username)
                .stream().map(TagResponse::new).toList();
    }

    @Transactional
    public List<Tag> saveAll(TagPostVO tagPostVO) {
        List<Tag> existTags = tagRepository.findByNameIn(tagPostVO.getTagNames());
        List<String> existTagNames = existTags.stream().map(Tag::getName).toList();
        List<String> notExistTagNames = tagPostVO.getTagNames().stream()
                .filter(tagName -> !existTagNames.contains(tagName))
                .toList();

        List<Tag> tags = notExistTagNames.stream()
                .map(tagName -> Tag.builder()
                        .name(tagName)
                        .build())
                .toList();

        List<Tag> savedTags = tagRepository.saveAll(tags);

        List<Tag> resultTags = new ArrayList<>();
        resultTags.addAll(existTags);
        resultTags.addAll(savedTags);
        return resultTags;
    }
}
