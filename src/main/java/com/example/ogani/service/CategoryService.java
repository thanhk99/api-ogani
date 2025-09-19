package com.example.ogani.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.ogani.models.Category;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.dtos.request.CreateCategoryRequest;
import com.example.ogani.repository.CategoryRepository;


@Service
public class CategoryService  {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findAll() {
        // TODO Auto-generated method stub
        List<Category> list = categoryRepository.findAll(Sort.by("id").descending());
        return list;
    }

    public Category createCategory(CreateCategoryRequest request) {
        // TODO Auto-generated method stub
        Category category = new Category();
        category.setName(request.getName());
        category.setEnable(true);
        categoryRepository.save(category);
        return category;
    }

    public Category updateCategory(long id, CreateCategoryRequest request) {
        // TODO Auto-generated method stub
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Category With Id: " + id));
        category.setName(request.getName());
        categoryRepository.save(category);
        return category;
    }

    public void enableCategory(long id) {
        // TODO Auto-generated method stub
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Category With Id: " + id));
        if(category.isEnable()){
            category.setEnable(false);
        } else{
            category.setEnable(true);
        }
        categoryRepository.save(category);
    }

public ResponseEntity<?> deleteCategory(long id) {
        Category category = categoryRepository.findByIdAndEnable(id,true).orElse(null);
        if(category == null){
            return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy danh mục với id: " + id));
        }

        category.setEnable(false);
        categoryRepository.save(category);
        return ResponseEntity.ok(Map.of("message", "Xóa danh mục thành công!"));
    }

    public List<Category> getListEnabled() {
        // TODO Auto-generated method stub
        List<Category> list = categoryRepository.findALLByEnabled();
        return list;
    }
    
}
