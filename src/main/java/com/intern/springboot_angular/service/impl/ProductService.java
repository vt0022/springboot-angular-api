package com.intern.springboot_angular.service.impl;


import com.intern.springboot_angular.entity.Product;
import com.intern.springboot_angular.repository.IProductRepository;
import com.intern.springboot_angular.service.IProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService implements IProductService {
    @Autowired
    IProductRepository productRepository;

    @Override
    public <S extends Product> S save(S entity) {
        return productRepository.save(entity);
    }

    @Override
    public Optional<Product> findById(Integer integer) {
        return productRepository.findById(integer);
    }

    @Override
    public Optional<Product> findByCode(String code) {
        return productRepository.findByCode(code);
    }

    @Override
    public void deleteById(Integer integer) {
        productRepository.deleteById(integer);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional
    @Override
    public void deleteByCode(String code) {
        productRepository.deleteByCode(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return productRepository.existsByCode(code);
    }
}
