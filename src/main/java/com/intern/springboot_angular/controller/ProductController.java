package com.intern.springboot_angular.controller;

import com.intern.springboot_angular.entity.Product;
import com.intern.springboot_angular.model.ProductModel;
import com.intern.springboot_angular.model.ResponseModel;
import com.intern.springboot_angular.service.IProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.sql.DataTruncation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class ProductController {

    @Autowired
    IProductService productService;

    @Autowired
    ModelMapper modelMapper;

    @GetMapping("")
    public ResponseEntity<?> listProducts(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "5") int size) {
        // Paging with page i and size 5
        Pageable pageable = PageRequest.of(page, size);
        // Get products
        Page<Product> products = productService.findAll(pageable);

        Page<ProductModel> productModels = products.map(
                product -> modelMapper.map(product, ProductModel.class)
        );
        return ResponseEntity.ok(productModels);
    }


    @GetMapping("/{code}")
    public ResponseEntity<?> getAProduct(@PathVariable String code) {
        // Find the product
        Optional<Product> product = productService.findByCode(code);
        if(product.isPresent()) {// check if we found the product
            ProductModel productModel = modelMapper.map(product.get(), ProductModel.class); // Map from entity to model
            // Set response
            ResponseModel responseModel = new ResponseModel(true, "Get successfully!", productModel);
            return ResponseEntity.ok(responseModel);
        }
        ResponseModel responseModel = new ResponseModel(false, "Product not found!", null);
        return new ResponseEntity<>(responseModel, HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    public ResponseEntity<?> addNewProduct(@RequestBody ProductModel productModel) {
        try {
            // Map from model to entity
            Product product = modelMapper.map(productModel, Product.class);
            product = productService.save(product);
            // Map back
            productModel = modelMapper.map(product, ProductModel.class);
            return ResponseEntity.ok(productModel);
        } catch (DataIntegrityViolationException e) {
            if (productService.existsByCode(productModel.getCode())) { // Duplicate
                ResponseModel responseModel = new ResponseModel(false, "Duplicate code", null);
                return ResponseEntity.badRequest().body(responseModel);
            }
            if ( productModel.getCode() == null ||
                    productModel.getName() == null ||
                    productModel.getCategory() == null||
                    productModel.getCode().isEmpty() ||
                    productModel.getName().isEmpty() ||
                    productModel.getCategory().isEmpty()) { // Null value
                ResponseModel responseModel = new ResponseModel(false, "Empty values are not allowed for code, name and category", null);
                return ResponseEntity.badRequest().body(responseModel);
            }
            ResponseModel responseModel = new ResponseModel(false, "Invalid numbers of characters allowed", null);
            return ResponseEntity.badRequest().body(responseModel);
        }
    }

    @PutMapping("/{code}")
    public ResponseEntity<?> updateAProduct(@PathVariable String code, @RequestBody ProductModel productModel) {
        try {
            // Find the current product
            Optional<Product> currentProduct = productService.findByCode(code);
            if (currentProduct.isPresent()) {// check if we found the product
                // Get the product before mapping to get new values
                Product product = currentProduct.get();
                modelMapper.map(productModel, product);
                product = productService.save(product);
                // Map to model to respond
                productModel = modelMapper.map(product, ProductModel.class); // Map from entity to model
                return ResponseEntity.ok(productModel);
            }
            return ResponseEntity.notFound().build();
        } catch (DataIntegrityViolationException e) {
            // Duplicate code except old code
            if (!code.equals(productModel.getCode()) && productService.existsByCode(productModel.getCode())) { // Duplicate
                ResponseModel responseModel = new ResponseModel(false, "Duplicate code", null);
                return ResponseEntity.badRequest().body(responseModel);
            }
            if ( productModel.getCode() == null ||
                    productModel.getName() == null ||
                    productModel.getCategory() == null||
                    productModel.getCode().isEmpty() ||
                    productModel.getName().isEmpty() ||
                    productModel.getCategory().isEmpty()) { // Null value
                ResponseModel responseModel = new ResponseModel(false, "Empty values are not allowed for code, name and category", null);
                return ResponseEntity.badRequest().body(responseModel);
            }
            ResponseModel responseModel = new ResponseModel(false, "Invalid numbers of characters allowed", null);
            return ResponseEntity.badRequest().body(responseModel);
        }
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<?> removeAProduct(@PathVariable String code) {
        ResponseModel responseModel = new ResponseModel();
        // Find the current product
        Optional<Product> currentProduct = productService.findByCode(code);
        if(currentProduct.isPresent()) {// check if we found the product
            productService.deleteByCode(code);
            responseModel.setSuccessfull(true);
            responseModel.setMessage("Successfully deleted");
            return ResponseEntity.ok(responseModel);
        }
        responseModel.setSuccessfull(false);
        responseModel.setMessage("Product not found");
        return new ResponseEntity<>(responseModel, HttpStatus.NOT_FOUND);
    }
}
