package com.intern.springboot_angular.service;

import com.intern.springboot_angular.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IProductService {

    <S extends Product> S save(S entity);

    Optional<Product> findById(Integer integer);

    Optional<Product> findByCode(String code);

    void deleteById(Integer integer);

    Page<Product> findAll(Pageable pageable);

    void deleteByCode(String code);

    boolean existsByCode(String code);
}
