package com.ruafashion.store.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductVariantRequest {

    private Long id;

    @NotNull(message = "Vui lòng chọn size")
    private Long sizeId;

    @NotBlank(message = "Màu sắc không được để trống")
    private String color;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được nhỏ hơn 0")
    private Integer quantity;
}