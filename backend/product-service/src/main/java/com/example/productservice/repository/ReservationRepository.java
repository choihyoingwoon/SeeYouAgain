package com.example.productservice.repository;

import com.example.productservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByProductId(Long productId);

    // 현재 대여하고 있는 품목 리스트
    @Query("SELECT r FROM Reservation r where r.lenderId = :lenderId AND r.startDate <= CURRENT_DATE AND r.endDate >= CURRENT_DATE")
    List<Reservation> findAllByLenderIdNow(@Param("lenderId") Long lenderId);

    // 현재 대여 완료한 품목 리스트
    @Query("SELECT r FROM Reservation r where r.lenderId = :lenderId AND r.endDate < CURRENT_DATE")
    List<Reservation> findAllByLenderIdEnd(@Param("lenderId") Long lenderId);

    // 현재 대여 예약중인 품목 리스트
    @Query("SELECT r FROM Reservation r where r.lenderId = :lenderId AND r.startDate > CURRENT_DATE")
    List<Reservation> findAllByLenderIdWaiting(@Param("lenderId") Long lenderId);

    // 현재 빌려주고 있는 내 품목 리스트
    @Query("SELECT r FROM Reservation r Where r.product.ownerId = :ownerId AND r.startDate <= CURRENT_DATE AND r.endDate >= CURRENT_DATE")
    List<Reservation> findAllByOwnerIdNow(@Param("ownerId") Long ownerId);

    // 앞으로 빌려줄 예정인 내 품목 리스트
    @Query("SELECT r FROM Reservation r Where r.product.ownerId = :ownerId AND r.startDate > CURRENT_DATE")
    List<Reservation> findAllByOwnerIdWaiting(@Param("ownerId") Long ownerId);

    // 숨긴 내 품목 리스트
    @Query("SELECT r FROM Reservation r Where r.product.ownerId = :ownerId AND r.product.isHide = true")
    List<Reservation> findAllByOwnerIdIsHidden(@Param("ownerId") Long ownerId);
}
