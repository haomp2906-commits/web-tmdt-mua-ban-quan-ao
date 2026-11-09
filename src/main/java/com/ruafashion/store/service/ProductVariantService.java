package com.ruafashion.store.service;


import com.ruafashion.store.dto.ProductVariantRequest;
import com.ruafashion.store.model.Product;
import com.ruafashion.store.model.ProductSize;
import com.ruafashion.store.model.Size;
import com.ruafashion.store.repository.ProductSizeRepository;
import com.ruafashion.store.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductSizeRepository productSizeRepository;
    private final SizeRepository sizeRepository;
    private final ProductService productService;

    public List<ProductSize> getVariantsByProductId(Long productId) {
        return productSizeRepository.findByProductIdOrderByIdAsc(productId);
    }

    public ProductSize getVariantById(Long id) {
        return productSizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));
    }

    public void saveVariant(Long productId, ProductVariantRequest request) {
        Product product = productService.getProductById(productId);

        Size size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy size"));

        String color = request.getColor().trim();

        ProductSize variant;

        if (request.getId() == null) {
            boolean exists = productSizeRepository
                    .existsByProductIdAndSizeIdAndColor(productId, request.getSizeId(), color);

            if (exists) {
                throw new RuntimeException(
                        "Biến thể có size và màu này đã tồn tại"
                );
            }

            variant = new ProductSize();
            variant.setProduct(product);

        } else {
            variant = getVariantById(request.getId());

            if (!variant.getProduct().getId().equals(productId)) {
                throw new RuntimeException("Biến thể không thuộc sản phẩm này");
            }
        }

        variant.setSize(size);
        variant.setColor(color);
        variant.setQuantity(request.getQuantity());

        productSizeRepository.save(variant);
    }

    public void deleteVariant(Long id) {
        ProductSize variant = getVariantById(id);
        productSizeRepository.delete(variant);
    }
}