package com.example.ogani.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.ogani.models.Blog;
import com.example.ogani.models.Image;
import com.example.ogani.models.Tag;
import com.example.ogani.models.User;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.dtos.request.CreateBlogRequest;
import com.example.ogani.repository.BlogRepository;
import com.example.ogani.repository.ImageRepository;
import com.example.ogani.repository.UserRepository;
import com.example.ogani.repository.TagRepository;
import com.example.ogani.service.BlogService;
import java.sql.Timestamp;


@Service
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> getList() {
        List<Blog> blogs = blogRepository.findAll(Sort.by("id").descending());
        return ResponseEntity.ok(blogs);
    }

    public ResponseEntity<?> getBlog(long id){
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Blog"));
        return ResponseEntity.ok(blog);
    }

    public ResponseEntity<?> createBlog(CreateBlogRequest request) {
        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setDescription(request.getDescription());
        blog.setContent(request.getContent());
        Image image = imageRepository.findById(request.getImageId()).orElseThrow(() -> new NotFoundException("Not Found Image"));
        blog.setImage(image);
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(()-> new NotFoundException("Not Found User"));
        blog.setUser(user);
        blog.setCreateAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
        Set<Tag> tags = new HashSet<>();
        for(Long tagId : request.getTags()){
            Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new NotFoundException("Not Found Tag"));
            tags.add(tag);
        }
        blog.setTags(tags);
        blogRepository.save(blog);
        return ResponseEntity.ok(Map.of( "message", "Create blog success"));
    }

    public ResponseEntity<?> updateBlog(long id, CreateBlogRequest request) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Blog"));
        blog.setTitle(request.getTitle());
        blog.setDescription(request.getDescription());
        blog.setContent(request.getContent());
        Image image = imageRepository.findById(request.getImageId()).orElseThrow(() -> new NotFoundException("Not Found Image"));
        blog.setImage(image);
        Set<Tag> tags = new HashSet<>();
        for(Long tagId : request.getTags()){
            Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new NotFoundException("Not Found Tag"));
            tags.add(tag);
        }
        blog.setTags(tags);
        blogRepository.save(blog);
        return ResponseEntity.ok(Map.of( "message", "Update blog success"));
    }

    public ResponseEntity<?> deleteBlog(long id) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Blog"));
        // blog.getTags().remove(this);
        blogRepository.delete(blog);
        return ResponseEntity.ok(Map.of( "message", "Delete blog success"));
    }

    public ResponseEntity<?> getListNewest(int limit) {
        List<Blog> list = blogRepository.getListNewest(limit);
        return ResponseEntity.ok(list);
    }


}
