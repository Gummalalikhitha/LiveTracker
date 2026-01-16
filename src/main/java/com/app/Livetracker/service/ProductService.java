//package com.app.Livetracker.service;
//
//import com.app.Livetracker.dto.productDTO;
//import com.app.Livetracker.entity.products;
//import com.app.Livetracker.repository.productRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import com.app.Livetracker.service.S3Service;
//import java.io.IOException;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class ProductService {
//
//    private final productRepository productRepository;
//    private final S3Service s3StorageService;
//
//    /* ===================== DTO → ENTITY ===================== */
//    private products toEntity(productDTO dto) {
//        products p = new products();
//        p.setPid(dto.getPid());
//        p.setPname(dto.getPname());
//        p.setDescription(dto.getDescription());
//        p.setPrice(dto.getPrice());
//        p.setStock(dto.getStock());
//        p.setPhoto(dto.getPhoto());
//        return p;
//    }
//
//    /* ===================== ENTITY → DTO ===================== */
//    private productDTO toDTO(products product) {
//        return new productDTO(
//                product.getPid(),
//                product.getPname(),
//                product.getDescription(),
//                product.getPrice(),
//                product.getStock(),
//                product.getPhoto()
//        );
//    }
//
//    /* ===================== CRUD ===================== */
//    public List<productDTO> getAllProducts() {
//        return productRepository.findAll()
//                .stream().map(this::toDTO)
//                .collect(Collectors.toList());
//    }
//
//    public productDTO getProduct(Long pid) {
//        return productRepository.findById(pid)
//                .map(this::toDTO)
//                .orElseThrow(() -> new RuntimeException("Product id " + pid + " doesn't exist"));
//    }
//
//    public void deleteProduct(Long pid) {
//        products product = productRepository.findById(pid)
//                .orElseThrow(() -> new RuntimeException("Product not found"));
//
//        // Delete image from S3
//        s3StorageService.delete(product.getPhoto());
//
//        // Delete product from DB
//        productRepository.delete(product);
//    }
//
//    public productDTO addProductWithImage(productDTO dto, MultipartFile imageFile) throws IOException {
//        if (imageFile == null || imageFile.isEmpty())
//            throw new RuntimeException("Product image is required");
//
//        products product = toEntity(dto);
//        products saved = productRepository.save(product);
//
//        // Upload image to S3 and store URL
//        String imageUrl = s3StorageService.upload(imageFile, "products").join();
//        saved.setPhoto(imageUrl);
//
//        products finalSaved = productRepository.save(saved);
//        return toDTO(finalSaved);
//    }
//
//    public productDTO updateProductWithImage(Long pid, productDTO dto, MultipartFile imageFile) throws IOException {
//        products existing = productRepository.findById(pid)
//                .orElseThrow(() -> new RuntimeException("Product not found"));
//
//        dto.setPid(pid);
//        dto.setPhoto(existing.getPhoto());
//
//        products product = toEntity(dto);
//
//        if (imageFile != null && !imageFile.isEmpty()) {
//            if (existing.getPhoto() != null) {
//                s3StorageService.delete(existing.getPhoto());
//            }
//            String imageUrl = s3StorageService.upload(imageFile, "products").join();
//            product.setPhoto(imageUrl);
//        }
//
//        products updated = productRepository.save(product);
//        return toDTO(updated);
//    }
//}



package com.app.Livetracker.service;

import com.app.Livetracker.dto.productDTO;
import com.app.Livetracker.entity.products;
import com.app.Livetracker.exception.BadRequestException;
import com.app.Livetracker.exception.ImageUploadException;
import com.app.Livetracker.exception.NotFoundException;
import com.app.Livetracker.repository.productRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final productRepository productRepository;
    private final S3Service s3StorageService;

    /* =====================================================
       🔐 ADMIN JWT VALIDATION (SERVICE LEVEL)
       ===================================================== */
    private void validateAdmin() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new BadRequestException("Unauthorized access");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new BadRequestException("Admin access required");
        }
    }

    /* ===================== DTO → ENTITY ===================== */
    private products toEntity(productDTO dto) {
        products p = new products();
        p.setPid(dto.getPid());
        p.setPname(dto.getPname());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        p.setPhoto(dto.getPhoto());
        return p;
    }

    /* ===================== ENTITY → DTO ===================== */
    private productDTO toDTO(products product) {
        return new productDTO(
                product.getPid(),
                product.getPname(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getPhoto()
        );
    }

    /* ===================== CRUD ===================== */

    public List<productDTO> getAllProducts() {
//        validateAdmin();
        return productRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    public productDTO getProduct(Long pid) {
//        validateAdmin();
        return productRepository.findById(pid)
                .map(this::toDTO)
                .orElseThrow(() ->
                        new NotFoundException("Product id " + pid + " doesn't exist"));
    }

    public void deleteProduct(Long pid) {
        validateAdmin();

        products product = productRepository.findById(pid)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        s3StorageService.delete(product.getPhoto());
        productRepository.delete(product);
    }

    public productDTO addProductWithImage(
            productDTO dto,
            MultipartFile imageFile) throws IOException {

        validateAdmin();

        if (imageFile == null || imageFile.isEmpty())
            throw new ImageUploadException("Product image is required");

        products product = toEntity(dto);
        products saved = productRepository.save(product);

        String imageUrl =
                s3StorageService.upload(imageFile, "products").join();

        saved.setPhoto(imageUrl);
        products finalSaved = productRepository.save(saved);

        return toDTO(finalSaved);
    }

    public productDTO updateProductWithImage(
            Long pid,
            productDTO dto,
            MultipartFile imageFile) throws IOException {

        validateAdmin();

        products existing = productRepository.findById(pid)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        dto.setPid(pid);
        dto.setPhoto(existing.getPhoto());

        products product = toEntity(dto);

        if (imageFile != null && !imageFile.isEmpty()) {
            if (existing.getPhoto() != null) {
                s3StorageService.delete(existing.getPhoto());
            }
            String imageUrl =
                    s3StorageService.upload(imageFile, "products").join();
            product.setPhoto(imageUrl);
        }

        products updated = productRepository.save(product);
        return toDTO(updated);
    }
}




