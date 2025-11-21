package com.danp1t.backend.service;

import com.danp1t.backend.dto.TagDTO;
import com.danp1t.backend.dto.TagDetailDTO;
import com.danp1t.backend.dto.PostSimpleDTO;
import com.danp1t.backend.model.Tag;
import com.danp1t.backend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    private TagDTO toDTO(Tag tag) {
        return new TagDTO(tag.getId(), tag.getName(), tag.getDescription());
    }

    private TagDetailDTO toDetailDTO(Tag tag) {
        List<PostSimpleDTO> posts = tag.getPosts().stream()
                .map(post -> new PostSimpleDTO(
                        post.getId(),
                        post.getTitle(),
                        post.getCreatedAt(),
                        post.getCountLike()
                ))
                .collect(Collectors.toList());

        return new TagDetailDTO(tag.getId(), tag.getName(), tag.getDescription(), posts);
    }

    private Tag toEntity(TagDTO dto) {
        Tag tag = new Tag();
        tag.setId(dto.getId());
        tag.setName(dto.getName());
        tag.setDescription(dto.getDescription());
        return tag;
    }

    public List<TagDTO> findAll() {
        return tagRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<TagDTO> findById(Integer id) {
        return tagRepository.findById(id).map(this::toDTO);
    }

    public Optional<TagDetailDTO> findByIdWithPosts(Integer id) {
        return tagRepository.findByIdWithPosts(id).map(this::toDetailDTO);
    }

    public TagDTO save(TagDTO tagDTO) {
        Tag tag = toEntity(tagDTO);
        Tag saved = tagRepository.save(tag);
        return toDTO(saved);
    }

    public TagDTO update(Integer id, TagDTO tagDTO) {
        if (!tagRepository.existsById(id)) {
            throw new RuntimeException("Tag not found with id: " + id);
        }
        Tag tag = toEntity(tagDTO);
        tag.setId(id);
        Tag updated = tagRepository.save(tag);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!tagRepository.existsById(id)) {
            throw new RuntimeException("Tag not found with id: " + id);
        }
        tagRepository.deleteById(id);
    }

    public List<TagDTO> findByNameContaining(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }
}