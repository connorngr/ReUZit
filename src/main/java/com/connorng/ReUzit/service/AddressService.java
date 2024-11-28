package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.Address;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserService userService;

    // Thêm địa chỉ mới
    public Address addAddress(Address address, String authenticatedEmail) {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }
        User authenticatedUser = userOptional.get();
        address.setUser(authenticatedUser);

        // Cập nhật tất cả các địa chỉ khác thành isDefault = false
        List<Address> existingAddresses = addressRepository.findByUserId(authenticatedUser.getId());
        for (Address existingAddress : existingAddresses) {
            if (!existingAddress.getId().equals(address.getId())) {
                existingAddress.setDefault(false);
                addressRepository.save(existingAddress);
            }
        }
        return addressRepository.save(address);
    }

    // Cập nhật địa chỉ
    public Address updateAddress(Long id, Address updatedAddress, String authenticatedEmail) {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }

        User authenticatedUser = userOptional.get();
        Optional<Address> optionalAddress = addressRepository.findById(id);

        if (optionalAddress.isPresent()) {
            Address existingAddress = optionalAddress.get();

            if (!existingAddress.getUser().getId().equals(authenticatedUser.getId())) {
                throw new SecurityException("You are not authorized to update this address.");
            }

            existingAddress.setFullName(updatedAddress.getFullName());
            existingAddress.setPhoneNumber(updatedAddress.getPhoneNumber());
            existingAddress.setStreet(updatedAddress.getStreet());
            existingAddress.setCity(updatedAddress.getCity());
            existingAddress.setProvince(updatedAddress.getProvince());
            existingAddress.setDistrict(updatedAddress.getDistrict());
            existingAddress.setWard(updatedAddress.getWard());
            existingAddress.setDefault(updatedAddress.isDefault());

            return addressRepository.save(existingAddress);
        } else {
            throw new RuntimeException("Address not found with id " + id);
        }
    }

    // Xoá địa chỉ
    public void deleteAddress(Long id, String authenticatedEmail) {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }

        User authenticatedUser = userOptional.get();
        Optional<Address> optionalAddress = addressRepository.findById(id);

        if (optionalAddress.isPresent()) {
            Address address = optionalAddress.get();

            if (!address.getUser().getId().equals(authenticatedUser.getId())) {
                throw new SecurityException("You are not authorized to delete this address.");
            }

            addressRepository.deleteById(id);
        } else {
            throw new RuntimeException("Address not found with id " + id);
        }
    }

    public Optional<Address> getDefaultAddress(String authenticatedEmail) {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }
        User authenticatedUser = userOptional.get();
        return addressRepository.findByUserIdAndIsDefaultTrue(authenticatedUser.getId());
    }

    public Address getAddressById(Long idAddress) {
        Optional<Address> addressOptional = addressRepository.findById(idAddress);
        if (!addressOptional.isPresent()) {
            throw new IllegalArgumentException("Address not found.");
        }
        return addressOptional.get();
    }


    // Lấy danh sách địa chỉ theo userId
    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    // Lấy tất cả địa chỉ của user theo authenticatedEmail
    public List<Address> getAddressesByAuthenticatedUser(String authenticatedEmail) {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }

        User authenticatedUser = userOptional.get();
        return addressRepository.findByUserId(authenticatedUser.getId());
    }

    public Address updateDefaultAddress(Long idUser, Long idAddress) {
        // Lấy danh sách tất cả các địa chỉ của người dùng
        List<Address> userAddresses = addressRepository.findByUserId(idUser);
        if (userAddresses.isEmpty()) {
            throw new IllegalArgumentException("No addresses found for the user.");
        }

        // Kiểm tra xem idAddress có thuộc người dùng này không
        Address newDefaultAddress = userAddresses.stream()
                .filter(address -> address.getId().equals(idAddress))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found for the user."));

        // Cập nhật isDefault cho các địa chỉ
        for (Address address : userAddresses) {
            address.setDefault(address.getId().equals(idAddress));
            addressRepository.save(address);
        }

        return newDefaultAddress; // Trả về địa chỉ mặc định mới
    }
}
