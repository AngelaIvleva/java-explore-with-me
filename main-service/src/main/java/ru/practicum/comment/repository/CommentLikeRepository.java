package ru.practicum.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.comment.model.Like;

@Repository
public interface CommentLikeRepository extends JpaRepository<Like, Long> {

    Like findByCommentIdAndUserId(Long commentId, Long userId);

    void deleteByCommentId(Long commentId);

}
