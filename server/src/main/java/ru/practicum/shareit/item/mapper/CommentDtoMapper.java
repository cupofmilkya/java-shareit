package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

@UtilityClass
public class CommentDtoMapper {

    public CommentDto toDto(Comment comment) {
        if (comment == null) return null;

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getName() : null)
                .created(comment.getCreated() != null ? comment.getCreated() : LocalDateTime.now())
                .build();
    }

    public Comment toEntity(CommentDto dto) {
        if (dto == null) return null;

        return Comment.builder()
                .id(dto.getId())
                .text(dto.getText())
                .created(dto.getCreated() != null ? dto.getCreated() : LocalDateTime.now())
                .build();
    }
}