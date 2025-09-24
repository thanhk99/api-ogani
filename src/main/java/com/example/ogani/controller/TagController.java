package com.example.ogani.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ogani.service.TagService;

import io.swagger.v3.oas.annotations.Operation;

import com.example.ogani.dtos.request.CreateTagRequest;

@RestController
@RequestMapping("/api/tag")
@CrossOrigin(origins = "*",maxAge = 3600)
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/")
    @Operation(summary="Lấy ra danh sách nhãn")
    public ResponseEntity<?> getList(){
       return tagService.getListTag();
    }
    @GetMapping("/enable")
    @Operation(summary="Lấy ra danh sách nhãn đã kích hoạt")
    public ResponseEntity<?> getListEnable(){
       return tagService.getListTagEnable();
    }

    @PostMapping("/create")
    @Operation(summary="Tạo mới nhãn")
    public ResponseEntity<?> createTag(@RequestBody CreateTagRequest request){
        return tagService.createTag(request);
      
    }

    @PutMapping("/update/{id}")
    @Operation(summary="Tìm nhãn bằng id và cập nhật nó")
    public ResponseEntity<?> updateTag(@PathVariable long id,@RequestBody CreateTagRequest request){
        return tagService.updateTag(id, request);

    }
    @PutMapping("/enable/{id}")
    @Operation(summary="Kích hoạt nhãn bằng id")
    public ResponseEntity<?> enabled(@PathVariable long id){
        return tagService.enableTag(id);

    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary="Xóa nhãn bằng id")
    public ResponseEntity<?> deleteTag(@PathVariable long id){
        return tagService.deleteTag(id);

    }
    
}
