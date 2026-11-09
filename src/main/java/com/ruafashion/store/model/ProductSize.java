package com.ruafashion.store.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "product_sizes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_product_size_color",
            columnNames = {"product_id", "size_id", "color"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id", nullable = false)
    private Size size;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(nullable = false)
    private Integer quantity = 0;
}