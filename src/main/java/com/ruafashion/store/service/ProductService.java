package com.ruafashion.store.service;


import com.ruafashion.store.model.Product;
import com.ruafashion.store.model.ProductImage;
import com.ruafashion.store.repository.CartItemRepository;
import com.ruafashion.store.repository.OrderDetailRepository;
import com.ruafashion.store.repository.ProductImageRepository;
import com.ruafashion.store.repository.ProductRepository;
import com.ruafashion.store.repository.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.ruafashion.store.model.ProductSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSizeRepository productSizeRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderDetailRepository orderDetailRepository;

    private static final String UPLOAD_DIR = "uploads/products/";

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchAndFilterProducts(
            String keyword,
            Long categoryId,
            String status,
            Double minPrice,
            Double maxPrice
    ) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        if (status != null && status.trim().isEmpty()) {
            status = null;
        }

        return productRepository.searchAndFilter(
                keyword,
                categoryId,
                status,
                minPrice,
                maxPrice
        );
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    public void saveProduct(Product product, MultipartFile[] imageFiles) {
        if (product.getStatus() == null || product.getStatus().trim().isEmpty()) {
            product.setStatus("ACTIVE");
        }

        if (product.getViewCount() == null) {
            product.setViewCount(0);
        }

        if (product.getCreatedAt() == null) {
            product.setCreatedAt(LocalDateTime.now());
        }

        Product savedProduct = productRepository.save(product);

        if (imageFiles != null && imageFiles.length > 0) {
            saveProductImages(savedProduct, imageFiles);
        }
    }

    private void saveProductImages(Product product, MultipartFile[] imageFiles) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            List<ProductImage> oldImages =
                    productImageRepository.findByProductId(product.getId());

            for (ProductImage oldImage : oldImages) {
                oldImage.setIsMain(false);
            }

            productImageRepository.saveAll(oldImages);

            boolean isFirstNewImage = true;

            for (MultipartFile file : imageFiles) {
                if (file.isEmpty()) {
                    continue;
                }

                String originalFileName = file.getOriginalFilename();
                String extension = "";

                if (originalFileName != null && originalFileName.contains(".")) {
                    extension = originalFileName.substring(
                            originalFileName.lastIndexOf(".")
                    );
                }

                String newFileName = UUID.randomUUID().toString() + extension;
                Path filePath = uploadPath.resolve(newFileName);

                Files.copy(file.getInputStream(), filePath);

                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl("/uploads/products/" + newFileName);

                if (isFirstNewImage) {
                    image.setIsMain(true);
                    product.setThumbnail("/uploads/products/" + newFileName);
                    isFirstNewImage = false;
                } else {
                    image.setIsMain(false);
                }

                productImageRepository.save(image);
            }

            productRepository.save(product);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload ảnh sản phẩm");
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        long orderDetailCount = orderDetailRepository.countByProductId(id);

        if (orderDetailCount > 0) {
            throw new RuntimeException(
                    "Không thể xóa sản phẩm vì sản phẩm đã tồn tại trong đơn hàng"
            );
        }

        cartItemRepository.deleteByProductId(id);
        productImageRepository.deleteByProductId(id);
        productSizeRepository.deleteByProductId(id);

        productRepository.deleteById(id);
    }
    public List<Product> getFeaturedProducts() {
        return productRepository
                .findTop6ByStatusOrderByCreatedAtDesc("ACTIVE");
    }
    public Page<Product> getCustomerProducts(
            String keyword,
            Long categoryId,
            Double minPrice,
            Double maxPrice,
            int page
    ) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                6,
                Sort.by("createdAt").descending()
        );

        return productRepository.findActiveProducts(
                keyword,
                categoryId,
                minPrice,
                maxPrice,
                pageable
        );
    }
    public Product getActiveProductById(Long id) {
        return productRepository.findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy sản phẩm"));
    }

    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    public List<ProductSize> getAvailableProductSizes(Long productId) {
        return productSizeRepository
                .findByProductIdAndQuantityGreaterThanOrderByIdAsc(
                        productId,
                        0
                );
    }
}