package com.danp1t.backend.controller;

import com.danp1t.backend.dto.ProductDTO;
import com.danp1t.backend.dto.ProductDetailDTO;
import com.danp1t.backend.dto.ProductInfoDTO;
import com.danp1t.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    // IS01: Отображение товаров с фильтрацией и сортировкой
    @GetMapping("/filtered")
    public ResponseEntity<List<ProductDetailDTO>> getProductsWithFilters(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        return ResponseEntity.ok(productService.findAllWithFilters(category, size, sortBy, sortOrder));
    }

    // IS12: Проверка наличия товара
    @GetMapping("/{id}/availability")
    public ResponseEntity<String> checkAvailability(@PathVariable Integer id) {
        String message = productService.getAvailabilityMessage(id);
        return ResponseEntity.ok(message);
    }

    // IS12: Детальная проверка наличия
    @GetMapping("/{id}/available")
    public ResponseEntity<Boolean> isProductAvailable(
            @PathVariable Integer id,
            @RequestParam(required = false) String size,
            @RequestParam(defaultValue = "1") Integer quantity) {
        boolean available = productService.isProductAvailable(id, size, quantity);
        return ResponseEntity.ok(available);
    }

    // IS14: Получение информации об остатках (для админов)
    @GetMapping("/stock")
    public ResponseEntity<List<ProductInfoDTO>> getStockInfo() {
        return ResponseEntity.ok(productService.getStockInfo());
    }

    // IS02: Просмотр конкретного товара с увеличением популярности
    @GetMapping("/{id}/view")
    public ResponseEntity<ProductDetailDTO> viewProduct(@PathVariable Integer id) {
        // Увеличиваем популярность при просмотре
        productService.incrementPopularity(id);

        return productService.findByIdWithProductInfos(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Остальные методы остаются
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ProductDetailDTO> getProductWithProductInfos(@PathVariable Integer id) {
        return productService.findByIdWithProductInfos(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String name) {
        return ResponseEntity.ok(productService.findByNameContaining(name));
    }

    // IS05: Добавление карточки товара (требуется роль OAPI:ROLE:PublishProduct)
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        if (productService.existsByName(productDTO.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        ProductDTO created = productService.save(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // IS07: Редактирование карточки товара (требуется роль OAPI:ROLE:EditProduct)
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Integer id, @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO updated = productService.update(id, productDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // IS06: Удаление карточки товара (требуется роль OAPI:ROLE:DeleteProduct)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        try {
            productService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Новый метод для создания товара с изображениями через FormData
    @PostMapping(value = "/create-with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> createProductWithImages(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "sizes", required = false) String sizesJson) {
        try {
            ProductDTO created = productService.createProductWithImages(productJson, images, sizesJson);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Загрузка дополнительных изображений для существующего товара
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProductImage(
            @PathVariable Integer id,
            @RequestParam("image") MultipartFile image) {
        try {
            String imagePath = productService.uploadProductImage(image, id);
            productService.addImageToProduct(id, imagePath);
            return ResponseEntity.ok(imagePath); // Возвращаем только путь, без префикса
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при загрузке изображения");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Удаление изображения товара
    @DeleteMapping("/{id}/images")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable Integer id,
            @RequestParam("imagePath") String imagePath) {
        try {
            productService.removeImageFromProduct(id, imagePath);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/images/{*path}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable String path) {
        try {
            byte[] image = productService.getProductImage(path);

            // Определяем Content-Type по расширению файла
            String contentType = determineContentType(path);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(image);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerPath.endsWith(".png")) {
            return "image/png";
        } else if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerPath.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg"; // по умолчанию
    }
}