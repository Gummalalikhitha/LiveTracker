package com.app.Livetracker.controller;

import com.app.Livetracker.dto.productDTO;
import com.app.Livetracker.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class ProductController {

    @Autowired
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/admin/products/all")
    public ResponseEntity<List<productDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/products/{pid}")
    public ResponseEntity<productDTO> getProduct(@PathVariable Long pid) {
        return ResponseEntity.ok(productService.getProduct(pid));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/products/delete/{pid}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long pid) {
        productService.deleteProduct(pid);
        return ResponseEntity.ok("Product deleted successfully!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/products/addimage", consumes = "multipart/form-data")
    public ResponseEntity<productDTO> addProductWithImage(
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        productDTO dto = objectMapper.readValue(productJson, productDTO.class);
        productDTO savedProduct = productService.addProductWithImage(dto, imageFile);
        return ResponseEntity.ok(savedProduct);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/admin/products/update/{pid}", consumes = "multipart/form-data")
    public ResponseEntity<productDTO> updateProductWithImage(
            @PathVariable Long pid,
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        productDTO dto = objectMapper.readValue(productJson, productDTO.class);
        productDTO updatedProduct = productService.updateProductWithImage(pid, dto, imageFile);
        return ResponseEntity.ok(updatedProduct);
    }

}



