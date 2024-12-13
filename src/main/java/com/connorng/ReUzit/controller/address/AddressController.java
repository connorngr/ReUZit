package com.connorng.ReUzit.controller;

import com.connorng.ReUzit.model.Address;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.service.AddressService;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserService userService;

    // Thêm địa chỉ mới
    @PostMapping
    public ResponseEntity<Address> addAddress(
            @RequestBody Address address) {
        String email = userService.getCurrentUserEmail();
        Address createdAddress = addressService.addAddress(address, email);
        return ResponseEntity.ok(createdAddress);
    }

    // Cập nhật địa chỉ theo id
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long id,
            @RequestBody Address updatedAddress) {
        String email = userService.getCurrentUserEmail();
        Address updated = addressService.updateAddress(id, updatedAddress, email);
        return ResponseEntity.ok(updated);
    }

    // Xoá địa chỉ theo id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id) {
        String email = userService.getCurrentUserEmail();
        addressService.deleteAddress(id, email);
        return ResponseEntity.noContent().build();
    }

    // Lấy danh sách địa chỉ theo userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getAddressesByUserId(@PathVariable Long userId) {
        List<Address> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/default")
    public ResponseEntity<Address> getDefaultAddress() {
        String email = userService.getCurrentUserEmail();
        Optional<Address> defaultAddress = addressService.getDefaultAddress(email);
        if (defaultAddress.isPresent()) {
            return ResponseEntity.ok(defaultAddress.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/default")
    public ResponseEntity<Address> updateDefaultAddress(
            @RequestParam Long idAddress) {
        String email = userService.getCurrentUserEmail();
        Optional<User> userOptional = userService.findByEmail(email);
        try {
            Address updatedDefaultAddress = addressService.updateDefaultAddress(userOptional.get().getId(), idAddress);
            return ResponseEntity.ok(updatedDefaultAddress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
