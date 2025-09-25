package com.example.ogani.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryRequest {
    @NotNull(message = "Tên danh mục rỗng")
    @NotEmpty(message = "Tên danh mục rỗng")
    @Size(min=1,max=50,message="Độ dài danh mục từ 1-50 ký tự")
    private String name;
}
