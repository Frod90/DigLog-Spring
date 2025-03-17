package api.store.diglog.repository;

import api.store.diglog.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findAllByPostsMemberUsernameOrderByName(String username);

    List<Tag> findByNameIn(List<String> names);
}
