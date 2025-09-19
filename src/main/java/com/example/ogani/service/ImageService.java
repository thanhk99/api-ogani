package com.example.ogani.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ogani.models.Image;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.repository.ImageRepository;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    public List<Image> getListImage() {
        return imageRepository.findAll();
    }

    public Image getImageById(long id) {
        Image image = imageRepository.findById(id).orElseThrow(() -> new NotFoundException("Image not found width id :" + id));

        return image;
    }

    public Image save(Image image) {
        return imageRepository.save(image);
    }

    public List<Image> getListByUser(long userId) {
        List<Image> images = imageRepository.getListImageOfUser(userId);
        return images;
    }

    public void deleteImage(long id) {
        
    }
}

