package com.example.productservice.service;

import com.example.productservice.client.UserServiceClient;
import com.example.productservice.dto.request.ProductRequestDto;
import com.example.productservice.dto.response.ProductClientResponseDto;
import com.example.productservice.dto.response.ProductResponseDto;
import com.example.productservice.dto.response.UserClientResponseDto;
import com.example.productservice.entity.*;
import com.example.productservice.exception.ApiException;
import com.example.productservice.exception.ExceptionEnum;
import com.example.productservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductImgRepository productImgRepository;

    private final ProductTagRepository productTagRepository;

    private final ReservationRepository reservationRepository;

    private final ReviewRepository reviewRepository;

    private final UserServiceClient userServiceClient;

    private final AmazonS3Service amazonS3Service;

    /**
     * explain : 제품 조회
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getDetailProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.PRODUCT_NOT_EXIST_EXCEPTION));

        List<ProductImg> productImgList = productImgRepository.findAllByProductId(productId);

        List<ProductTag> productTagList = productTagRepository.findAllByProductId(productId);

        List<Reservation> reservationList = reservationRepository.findAllByProductId(productId);

        List<HashMap<String, LocalDate>> reservationMapList = getReservationPeriod(reservationList);

        List<Review> reviewList = reviewRepository.findAllByProductId(productId);

        double totalScore = getReviewScoreAvg(reviewList);

        UserClientResponseDto userInfo = userServiceClient.getUserInfo(product.getOwnerId()).getData();

        return ProductResponseDto.of(product, productImgList, productTagList, reservationMapList, totalScore, userInfo);
    }

    /**
     * explain : 제품 등록 + 태그, 이미지 등록
     */
    @Override
    @Transactional
    public void createProduct(Long userId, ProductRequestDto requestDto) {
        Product product = Product.of(userId ,requestDto, true, 0, LocalDateTime.now());
        productRepository.save(product);

        List<ProductTag> productTagList = requestDto.getTag().stream().map(t -> ProductTag.of(product, t)).collect(toList());
        productTagRepository.saveAll(productTagList);

        List<ProductImg> productImgList = saveProductImg(requestDto, product);
        productImgRepository.saveAll(productImgList);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductClientResponseDto getProductInfo(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.PRODUCT_NOT_EXIST_EXCEPTION));

        ProductImg productImg = productImgRepository.findAllByProductId(productId).get(0);

        return ProductClientResponseDto.of(product, productImg.getProductImg());
    }

    /**
     * explain : 제품 수정 + 태그, 이미지 수정
     */
    @Override
    @Transactional
    public void updateProduct(Long userId, Long productId, ProductRequestDto requestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.PRODUCT_NOT_EXIST_EXCEPTION));

        if (!product.getOwnerId().equals(userId)) throw new ApiException(ExceptionEnum.OWNER_NOT_MATCH_EXCEPTION);

        // 제품 수정
        product.updateProduct(requestDto);

        // 제품 태그 삭제 후 생성 => querydsl을 사용하면 더 효율적일지도?
        productTagRepository.deleteAllByProductId(productId);

        List<ProductTag> newProductTagList = requestDto.getTag().stream().map(t -> ProductTag.of(product, t)).collect(toList());
        productTagRepository.saveAll(newProductTagList);

        // 제품 이미지 삭제 후 생성
        List<ProductImg> productImgList = productImgRepository.findAllByProductId(productId);
        deleteProductImg(productImgList);
        productImgRepository.deleteAllByProductId(productId);

        List<ProductImg> newProductImgList = saveProductImg(requestDto, product);
        productImgRepository.saveAll(newProductImgList);
    }

    /**
     * explain : 제품 삭제
     */
    @Override
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.PRODUCT_NOT_EXIST_EXCEPTION));

        if (!product.getOwnerId().equals(userId)) throw new ApiException(ExceptionEnum.OWNER_NOT_MATCH_EXCEPTION);

        productRepository.delete(product);
    }

    private void deleteProductImg(List<ProductImg> productImgList) {
        productImgList.forEach(i -> {
            amazonS3Service.delete(i.getProductKey());
        });
    }

    private List<ProductImg> saveProductImg(ProductRequestDto requestDto, Product product) {
        return requestDto.getProductImg().stream()
                .map(i -> {
                    String fileName = saveProductImg(i);
                    String fileUrl = amazonS3Service.getFileUrl(fileName);
                    return ProductImg.of(product, fileName, fileUrl);
                })
                .collect(toList());
    }


    private String saveProductImg(MultipartFile i) {
        try {
            return amazonS3Service.upload(i, "product");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double getReviewScoreAvg(List<Review> reviewList) {
        int cnt = reviewList.size();
        int totalScore = 0;
        for (Review review : reviewList) {
            totalScore += review.getReviewScore();
        }
        return (double) totalScore / cnt;
    }

    private List<HashMap<String, LocalDate>> getReservationPeriod(List<Reservation> reservationList) {
        return reservationList.stream()
                .map(r -> {
                    HashMap<String, LocalDate> reservationMap = new HashMap<>();
                    reservationMap.put("startDate", r.getStartDate());
                    reservationMap.put("endDate", r.getEndDate());
                    return reservationMap;
                })
                .collect(toList());
    }
}