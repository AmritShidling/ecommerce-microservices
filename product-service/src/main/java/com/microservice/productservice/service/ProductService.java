package com.microservice.productservice.service;

import com.microservice.productservice.dto.ProductRequest;
import com.microservice.productservice.dto.ProductResponse;
import com.microservice.productservice.model.Product;
import com.microservice.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    @Autowired
    ProductRepository productRepository;
    public void createProduct(ProductRequest productRequest){
        Product product = Product.builder()
                .name(productRequest.getName())
                .price(productRequest.getPrice())
                .description((productRequest.getDescription())).build();
        productRepository.save(product);
        log.info("Product {} is saved",product.getId());
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> proucts =  productRepository.findAll();
        return proucts.stream().map(product -> getProductRepose(product)).toList();
    }

    private ProductResponse getProductRepose(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
   }
}
