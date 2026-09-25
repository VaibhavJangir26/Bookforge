package com.bluewave.repo;

import com.bluewave.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PaymentRepo extends JpaRepository<Payment,String> {

    Optional<Payment> findByBookingId(String id);

    Optional<Payment> findByStripePaymentIntentId(String id);
}
