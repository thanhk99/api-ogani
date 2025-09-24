package com.example.ogani.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.ogani.models.Blog;
import com.example.ogani.models.Tag;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.dtos.request.CreateTagRequest;
import com.example.ogani.repository.BlogRepository;
import com.example.ogani.repository.TagRepository;
import com.example.ogani.service.TagService;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private BlogRepository blogRepository;

    public ResponseEntity<?> getListTag() {
        List<Tag> tags = tagRepository.findAll(Sort.by("id").descending());
        return ResponseEntity.ok(tags);
    }

    public ResponseEntity<?> getListTagEnable() {
        List<Tag> tags = tagRepository.findByEnableTrue()
                .stream()
                .sorted(Comparator.comparingLong(Tag::getId).reversed())
                .toList();
        return ResponseEntity.ok(tags);
    }

    public ResponseEntity<?> createTag(CreateTagRequest request) {
        Tag tagExists = tagRepository.findByName(request.getName());
        if (tagExists != null && tagExists.isEnable()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tag is already"));
        } else if (tagExists != null && !tagExists.isEnable()) {
            tagExists.setEnable(true);
            tagRepository.save(tagExists);
            return ResponseEntity.ok(Map.of("message", "Create tag success"));
        } else {
            Tag tag = new Tag();
            tag.setName(request.getName());
            tag.setEnable(false);
            tagRepository.save(tag);
            return ResponseEntity.ok(Map.of("message", "Create tag success"));
        }

    }

    public ResponseEntity<?> updateTag(long id, CreateTagRequest request) {

        Tag tag = tagRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Foud Tag"));
        tag = tagRepository.findByName(request.getName());
        if (tag != null && tag.getId() != id && tag.isEnable()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tag is already"));
        }
        tag.setName(request.getName());
        tagRepository.save(tag);
        return ResponseEntity.ok(Map.of("message", "Update tag success"));
    }

    public ResponseEntity<?> deleteTag(long id) {
        Tag tag = tagRepository.findById(id).orElse(null);
        if (tag == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tag not found"));
        }
        // Xóa liên kết giữa Tag và Blog
        List<Blog> blogs = blogRepository.findAllByTags_Id(id);
        for (Blog blog : blogs) {
            blog.getTags().remove(tag);
            blogRepository.save(blog);
        }
        // Xóa cứng Tag
        tagRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Delete tag success"));
    }

    public ResponseEntity<?> enableTag(long id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Tag With Id: " + id));
        if (tag.isEnable()) {
            tag.setEnable(false);
            tagRepository.save(tag);
            return ResponseEntity.ok(Map.of("message", "Disable tag success"));
        } else {
            tag.setEnable(true);
            tagRepository.save(tag);
            return ResponseEntity.ok(Map.of("message", "Enable tag success"));
        }
    }

}
