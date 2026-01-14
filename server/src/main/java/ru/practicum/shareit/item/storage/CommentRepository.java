package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemIdAndAuthorId(long itemId, long authorId);

    List<Comment> findByItemId(long itemId);

    List<Comment> findByItemIdOrderByCreatedDesc(long itemId);
}
