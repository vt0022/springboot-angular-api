package com.intern.springboot_angular.repository;

import com.intern.springboot_angular.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findByCode(String code);

    void deleteByCode(String code);

    boolean existsByCode(String code);
}
