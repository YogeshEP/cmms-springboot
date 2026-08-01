package com.cmms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spare_parts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Part name is required")
    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String partNumber;

    private Integer quantityInStock = 0;

    private Integer reorderLevel = 5;

    private Double unitCost;
}
