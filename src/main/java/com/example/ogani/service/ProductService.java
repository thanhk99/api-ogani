package com.example.ogani.service;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.ogani.models.Category;
import com.example.ogani.models.Image;
import com.example.ogani.models.Product;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.dtos.request.CreateProductRequest;
import com.example.ogani.dtos.response.ProductResponse;
import com.example.ogani.repository.CategoryRepository;
import com.example.ogani.repository.ImageRepository;
import com.example.ogani.repository.ProductRepository;

@Service
public class ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageRepository imageRepository;

    public ResponseEntity<?>  getList() {
        List<Product> products= productRepository.findAll(Sort.by("id").descending());
        List<ProductResponse> productResponses = new ArrayList<>();
        for(Product product: products){
            ProductResponse productResponse = new ProductResponse();
            productResponse.setName(product.getName());
            productResponse.setImage(product.getImages().iterator().next().getData());
            productResponse.setPrice(product.getPrice());
            productResponse.setQuantity(product.getQuantity());
            productResponse.setCategory(product.getCategory().getName());
            productResponse.setDescription(product.getDescription());
            productResponse.setContent(product.getContent());
            productResponses.add(productResponse);
        }
        return ResponseEntity.ok(productResponses);
    }

    public Product getProduct(long id) {
        Product product= productRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Product With Id: " + id));

        return product;
    }
    

    public Product createProduct(CreateProductRequest request) {
        // Kiểm tra categoryId
        if (request.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Invalid category ID: " + request.getCategoryId());
        }

        // Kiểm tra imageIds (nếu cần)
        if (request.getImageIds() != null && request.getImageIds().stream().anyMatch(id -> id <= 0)) {
            throw new IllegalArgumentException("Invalid image ID(s) in request");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setContent(request.getContent());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        // Tìm category
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new NotFoundException("Not Found Category With Id: " + request.getCategoryId()));
        product.setCategory(category);

        // Tìm images
        Set<Image> images = new HashSet<>();
        if (request.getImageIds() != null) {
            for (long imageId : request.getImageIds()) {
                Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new NotFoundException("Not Found Image With Id: " + imageId));
                images.add(image);
            }
        }
        product.setImages(images);

        // Lưu product
        return productRepository.save(product);
    }

    public Product updateProduct(long id, CreateProductRequest request) {
        Product product= productRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Product With Id: " + id));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setContent(request.getContent());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(()-> new NotFoundException("Not Found Category With Id: " + request.getCategoryId()));
        product.setCategory(category);

        Set<Image> images = new HashSet<>();
        for(long imageId: request.getImageIds()){
            Image image = imageRepository.findById(imageId).orElseThrow(() -> new NotFoundException("Not Found Image With Id: " + imageId));
            images.add(image);
        }
        product.setImages(images);
        productRepository.save(product);

        return product;
    }

    public void deleteProduct(long id) {
        Product product= productRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Product With Id: " + id));
        product.getImages().remove(this);
        productRepository.delete(product);
    }

    public List<Product> getListNewst(int number) {
        List<Product> list = productRepository.getListNewest(number);
        return list;
    }

    public List<Product> getListByPrice() {
        return productRepository.getListByPrice();
    }

    public List<Product> findRelatedProduct(long id){
        List<Product> list = productRepository.findRelatedProduct(id);
        return list;

    }

    public List<Product> getListProductByCategory(long id){
        List<Product> list =productRepository.getListProductByCategory(id);
        return list;
    }

    public List<Product> getListByPriceRange(long id,int min, int max){
        List<Product> list =productRepository.getListProductByPriceRange(id, min, max);
        return list;
    }

    public List<Product> searchProduct(String keyword) {
        List<Product> list = productRepository.searchProduct(keyword);
        return list;
    }


    
}
