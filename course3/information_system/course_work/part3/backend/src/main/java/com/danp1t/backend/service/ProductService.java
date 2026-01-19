package com.danp1t.backend.service;

import com.danp1t.backend.dto.ProductDTO;
import com.danp1t.backend.dto.ProductDetailDTO;
import com.danp1t.backend.dto.ProductInfoDTO;
import com.danp1t.backend.model.Product;
import com.danp1t.backend.model.ProductInfo;
import com.danp1t.backend.repository.ProductInfoRepository;
import com.danp1t.backend.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private static final String UPLOAD_SUB_DIR = "products";

    private ProductDTO toDTO(Product product) {
        List<String> images = new ArrayList<>();
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            images = parseImagePaths(product.getImages());
        }

        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getBasePrice(),
                product.getPopularity(),
                images,
                null
        );
    }

    private ProductDetailDTO toDetailDTO(Product product) {
        List<ProductInfoDTO> productInfos = product.getProductInfos().stream()
                .map(productInfo -> new ProductInfoDTO(
                        productInfo.getId(),
                        productInfo.getProduct().getId(),
                        productInfo.getProduct().getName(),
                        productInfo.getSizeName(),
                        productInfo.getCountItems(),
                        productInfo.getPrice()
                ))
                .collect(Collectors.toList());

        List<String> images = new ArrayList<>();
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            images = parseImagePaths(product.getImages());
        }

        boolean inStock = product.getProductInfos().stream()
                .anyMatch(pi -> pi.getCountItems() > 0);

        String availabilityMessage;
        if (!inStock) {
            availabilityMessage = "Товар отсутствует на складе";
        } else {
            long availableSizes = product.getProductInfos().stream()
                    .filter(pi -> pi.getCountItems() > 0)
                    .count();
            availabilityMessage = "Товар доступен в " + availableSizes + " размерах";
        }

        return new ProductDetailDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getBasePrice(),
                product.getPopularity(),
                images,
                productInfos,
                inStock,
                availabilityMessage
        );
    }

    private List<String> parseImagePaths(String imagesString) {
        if (imagesString == null || imagesString.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(imagesString.split(","))
                .map(String::trim)
                .filter(path -> !path.isEmpty())
                .map(this::toImageUrl)
                .collect(Collectors.toList());
    }

    private String toImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }

        String normalizedPath = relativePath.replace('\\', '/');
        return "/api/products/images/" + normalizedPath;
    }

    private Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setBasePrice(dto.getBasePrice());
        product.setPopularity(dto.getPopularity() != null ? dto.getPopularity() : 0);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<String> relativePaths = dto.getImages().stream()
                    .map(this::toRelativePath)
                    .collect(Collectors.toList());
            product.setImages(String.join(",", relativePaths));
        }

        return product;
    }

    private String toRelativePath(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }

        String prefix = "/api/products/images/";
        if (imageUrl.startsWith(prefix)) {
            String relativePath = imageUrl.substring(prefix.length());
            return relativePath.replace('/', '\\');
        }

        return imageUrl;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String uploadProductImage(MultipartFile file, Integer productId) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Файл пустой");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Разрешены только изображения");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Размер файла не должен превышать 5MB");
        }

        String subDirectory = UPLOAD_SUB_DIR + "/product_" + productId;
        return fileStorageService.storeFile(file, subDirectory);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeImageFromProduct(Integer productId, String imagePath) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String relativePath = toRelativePath(imagePath);

            List<String> images = new ArrayList<>(Arrays.asList(product.getImages().split(",")));
            images.remove(relativePath);
            product.setImages(!images.isEmpty() ? String.join(",", images) : null);
            productRepository.save(product);
            fileStorageService.deleteFile(relativePath);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ProductDTO createProductWithImages(String productJson, List<MultipartFile> images, String sizesJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductDTO productDTO = objectMapper.readValue(productJson, ProductDTO.class);

        if (existsByName(productDTO.getName())) {
            throw new RuntimeException("Товар с таким названием уже существует");
        }

        Product product = toEntity(productDTO);
        product.setId(null);
        product.setPopularity(0);
        Product savedProduct = productRepository.save(product);

        List<String> imagePaths = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imagePath = uploadProductImage(image, savedProduct.getId());
                    imagePaths.add(imagePath);
                }
            }
        }

        if (!imagePaths.isEmpty()) {
            savedProduct.setImages(String.join(",", imagePaths));
            savedProduct = productRepository.save(savedProduct);
        }

        if (sizesJson != null && !sizesJson.trim().isEmpty()) {
            List<Map<String, Object>> sizesList = objectMapper.readValue(sizesJson, List.class);

            for (Map<String, Object> sizeData : sizesList) {
                ProductInfo productInfo = new ProductInfo();
                productInfo.setProduct(savedProduct);
                productInfo.setSizeName((String) sizeData.get("sizeName"));
                productInfo.setPrice(((Number) sizeData.get("price")).intValue());
                productInfo.setCountItems(((Number) sizeData.get("countItems")).intValue());

                productInfoRepository.save(productInfo);
            }
        }

        return toDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductDetailDTO> findAllWithFilters(String category, String size, String sortBy, String sortOrder) {
        List<Product> products = productRepository.findAll();

        if (category != null && !category.isEmpty()) {
            products = products.stream()
                    .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                    .collect(Collectors.toList());
        }

        if (size != null && !size.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getProductInfos().stream()
                            .anyMatch(pi -> size.equalsIgnoreCase(pi.getSizeName())))
                    .collect(Collectors.toList());
        }

        if (sortBy != null && !sortBy.isEmpty()) {
            Comparator<Product> comparator = getComparator(sortBy);

            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }

            products = products.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        return products.stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }

    private Comparator<Product> getComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "name":
                return Comparator.comparing(Product::getName);
            case "popularity":
                return Comparator.comparing(Product::getPopularity);
            case "price":
                return Comparator.comparing(Product::getBasePrice, Comparator.nullsFirst(Comparator.naturalOrder()));
            default:
                return Comparator.comparing(Product::getId);
        }
    }

    @Transactional(readOnly = true)
    public boolean isProductAvailable(Integer productId, String size, Integer quantity) {
        return productRepository.findById(productId)
                .map(product -> product.getProductInfos().stream()
                        .filter(pi -> size == null || size.equals(pi.getSizeName()))
                        .anyMatch(pi -> pi.getCountItems() >= quantity))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public String getAvailabilityMessage(Integer productId) {
        return productRepository.findById(productId)
                .map(product -> {
                    long availableSizes = product.getProductInfos().stream()
                            .filter(pi -> pi.getCountItems() > 0)
                            .count();

                    if (availableSizes == 0) {
                        return "Товар отсутствует на складе";
                    } else {
                        return "Товар доступен в " + availableSizes + " размерах";
                    }
                })
                .orElse("Товар не найден");
    }

    @Transactional(readOnly = true)
    public List<ProductInfoDTO> getStockInfo() {
        return productRepository.findAll().stream()
                .flatMap(product -> product.getProductInfos().stream()
                        .map(pi -> new ProductInfoDTO(
                                pi.getId(),
                                pi.getProduct().getId(),
                                pi.getProduct().getName(),
                                pi.getSizeName(),
                                pi.getCountItems(),
                                pi.getPrice()
                        )))
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void incrementPopularity(Integer productId) {
        productRepository.findById(productId)
                .ifPresent(product -> {
                    product.setPopularity(product.getPopularity() + 1);
                    productRepository.save(product);
                });
    }

    @Transactional(readOnly = true)
    public byte[] getProductImage(String path) throws IOException {
        String relativePath = toRelativePath(path);
        String normalizedPath = relativePath.replace('/', '\\');
        Path filePath = Paths.get("uploads", normalizedPath).toAbsolutePath().normalize();
        if (!Files.exists(filePath)) {
            throw new IOException("Файл не найден: " + filePath);
        }

        byte[] data = Files.readAllBytes(filePath);
        return data;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ProductDTO> findById(Integer id) {
        return productRepository.findById(id)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<ProductDetailDTO> findByIdWithProductInfos(Integer id) {
        return productRepository.findByIdWithProductInfos(id)
                .map(this::toDetailDTO);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ProductDTO save(ProductDTO productDTO) {
        Product product = toEntity(productDTO);
        product.setId(null);
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ProductDTO update(Integer id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (productDTO.getName() != null) {
            existingProduct.setName(productDTO.getName());
        }
        if (productDTO.getDescription() != null) {
            existingProduct.setDescription(productDTO.getDescription());
        }
        if (productDTO.getCategory() != null) {
            existingProduct.setCategory(productDTO.getCategory());
        }
        if (productDTO.getBasePrice() != null) {
            existingProduct.setBasePrice(productDTO.getBasePrice());
        }
        if (productDTO.getPopularity() != null) {
            existingProduct.setPopularity(productDTO.getPopularity());
        }
        if (productDTO.getImages() == null || productDTO.getImages().isEmpty()) {
            existingProduct.setImages(null);
        }
        if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
            String processedImages = productDTO.getImages().stream()
                    .map(imageUrl -> {
                        String cleaned = imageUrl.replace("/api/products/images/", "");
                        cleaned = cleaned.replace("/api/products/images/", "");
                        return cleaned;
                    })
                    .collect(Collectors.joining(","));
            existingProduct.setImages(processedImages);
        }

        Product updated = productRepository.save(existingProduct);
        return toDTO(updated);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteById(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findByNameContaining(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addImageToProduct(Integer productId, String imagePath) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден с id: " + productId));

        List<String> images = new ArrayList<>();
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String[] imageArray = product.getImages().split(",");
            images.addAll(Arrays.asList(imageArray));
        }

        images.add(imagePath);
        product.setImages(String.join(",", images));
        productRepository.save(product);
    }
}