package ru.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.comment.enums.CommentStatus;
import ru.practicum.comment.model.Comment;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByAuthorId(Long userId, Pageable pageable);

    List<Comment> findAllByAuthorIdAndStatus(Long userId,
                                             CommentStatus status,
                                             Pageable pageable);

    List<Comment> findAllByEventId(Long eventId,
                                   Pageable pageable);

    List<Comment> findAllByEventIdAndStatus(Long eventId,
                                            CommentStatus status,
                                            Pageable pageable);

    Optional<Comment> findByIdAndAuthor(Long commentId,
                                        User user);

    Optional<Comment> findByIdAndStatus(Long commentId,
                                        CommentStatus status);

    List<Comment> findByReplyIdAndStatus(Long commentId,
                                         CommentStatus status,
                                         Pageable pageable);

    void deleteByReplyId(Long commentId);
}
