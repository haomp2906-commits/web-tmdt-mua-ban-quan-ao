package com.ruafashion.store.model;

//sản phẩm//

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "discount_price")
    private Double discountPrice;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "material", length = 100)
    private String material;

    @Column(name = "thumbnail", length = 255)
    private String thumbnail;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "status", length = 50)
    private String status = "ACTIVE";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}