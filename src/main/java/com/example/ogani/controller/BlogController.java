package com.example.ogani.controller;

import org.springframework.http.ResponseEntity;
import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ogani.dtos.request.CreateBlogRequest;
import com.example.ogani.service.BlogService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/blog")
@CrossOrigin(origins = "*",maxAge = 3600)
public class BlogController {

    @Autowired
    private BlogService blogService;

    @GetMapping("/")
    @Operation(summary="Lấy tất cả danh sách blog")
    public ResponseEntity<?> getList(){
       return blogService.getList();
    }

    @GetMapping("/{id}")
    @Operation(summary="Lấy ra blog bằng ID")
    public ResponseEntity<?> getBlog(@PathVariable long id){
        
        return blogService.getBlog(id);


    }

    @GetMapping("/newest")
    @Operation(summary="Lấy ra danh sách blog mới nhất với số lượng = limit")
    public ResponseEntity<?> getListNewest(@RequestParam int limit){
        return blogService.getListNewest(limit);

    }


    @PostMapping("/create")
    @Operation(summary="Tạo mới blog")
    public ResponseEntity<?> create(@RequestBody CreateBlogRequest request){

        return blogService.createBlog(request);

    }

    @PutMapping("/update/{id}")
    @Operation(summary="Tìm blog bằng id và cập nhật blog đó")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody CreateBlogRequest request){

       return blogService.updateBlog(id, request);

    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary="Xóa blog bằng Id")
    public ResponseEntity<?> delete(@PathVariable long id){
        return blogService.deleteBlog(id);
        
    }
    
}
