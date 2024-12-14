package com.lcaohoanq.nocket.domain.cart;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {
    
    Boolean existsByUserId(Long userId);
    
    Optional<Cart> findByUserId(Long userId);
    
    @Modifying
    @Query("UPDATE Cart c SET c.total = c.total + :quantity WHERE c.id = :productId")
    void increase(
        @Param("productId") Long cartId,
        @Param("quantity") Integer quantity
    );
    
    @Modifying
    @Query("UPDATE Cart c SET c.total = c.total - :quantity WHERE c.id = :productId")
    void decrease(
        @Param("productId") Long cartId,
        @Param("quantity") Integer quantity
    );

}
