package com.connorng.ReUzit.controller.payment;

import com.connorng.ReUzit.model.Address;
import com.connorng.ReUzit.model.Listing;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Listing listing;
    private Address shippingAddress;
}
