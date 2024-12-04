package com.connorng.ReUzit.service;

import com.connorng.ReUzit.dto.ChatMessageDTO;
import com.connorng.ReUzit.dto.ListingChatDTO;
import com.connorng.ReUzit.model.ChatMessage;
import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.ChatMessageRepository;
import com.connorng.ReUzit.repository.ListingRepository;
import com.connorng.ReUzit.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    private final UserRepository userRepository;

    private final ListingRepository listingRepository;

    private static final String UPLOAD_DIR = "uploadFiles/";

    private final SimpMessagingTemplate messagingTemplate;

    public List<String> uploadFiles(MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, file.getBytes());
                fileUrls.add("/uploadFiles/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("File upload failed", e);
            }
        }
        return fileUrls;
    }

    public ChatMessageDTO saveMessage(ChatMessageDTO chatMessageDTO) throws IOException {
        userRepository.findById(chatMessageDTO.getSenderId()).orElseThrow(() -> new EntityNotFoundException("Sender not found"));
        userRepository.findById(chatMessageDTO.getReceiverId()).orElseThrow(() -> new EntityNotFoundException("Receiver not found"));
        listingRepository.findById(chatMessageDTO.getListingId()).orElseThrow(() -> new EntityNotFoundException("Listing not found"));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(chatMessageDTO.getSenderId());
        chatMessage.setReceiverId(chatMessageDTO.getReceiverId());
        chatMessage.setListingId(chatMessageDTO.getListingId());
        chatMessage.setContent(chatMessageDTO.getContent());
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setRead(false);
        // Nếu có fileUrls, lưu vào message
        if (chatMessageDTO.getFileUrls() != null) {
            chatMessage.setFileUrls(chatMessageDTO.getFileUrls());
        }

        ChatMessage saved = chatMessageRepository.save(chatMessage);
        ChatMessageDTO result = convertToDTO(saved);
        result.setChatKey(chatMessageDTO.getChatKey());
        result.setOtherUserName(chatMessageDTO.getOtherUserName());
        // Gửi tin nhắn qua WebSocket ngay sau khi lưu
        messagingTemplate.convertAndSend("/topic/messages", result);
        return result;
    }

    public List<ChatMessageDTO> getMessages(Long senderId, Long receiverId, Long listingId, Long before, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("timestamp").descending());
        List<ChatMessage> messages;
        if (before != null) {
            messages = chatMessageRepository.findByListingIdAndParticipants(
                    listingId, senderId, receiverId, before, pageable);
        } else {
            messages = chatMessageRepository.findByListingIdAndParticipants(
                    listingId, senderId, receiverId, pageable);
        }
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ListingChatDTO> getAllUserChats(Long userId) {
        // Lấy tất cả listing mà user là chủ sở hữu
        List<Listing> userListings = listingRepository.findByUserId(userId);

        Map<String, ListingChatDTO> chatMap = new HashMap<>();

        // Xử lý các listing mà user sở hữu
        for (Listing listing : userListings) {
            // Lấy tất cả người đã chat với listing này
            List<Long> chatters = chatMessageRepository
                    .findDistinctSendersByListingAndReceiver(listing.getId(), userId);

            for (Long chatterId : chatters) {
                String chatKey = listing.getId() + "_" + chatterId;
                if (!chatMap.containsKey(chatKey)) {
                    User chatter = userRepository.findById(chatterId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found"));

                    ListingChatDTO chatDTO = createListingChatDTO(listing, chatter, true);
                    // Lấy tin nhắn cuối cùng và số tin chưa đọc
                    updateLastMessageAndUnreadCount(chatDTO, listing.getId(), userId, chatterId);
                    chatMap.put(chatKey, chatDTO);
                }
            }
        }

        // Xử lý các listing mà user đã chat (không phải chủ sở hữu)
        List<Object[]> chattersData = chatMessageRepository
                .findDistinctListingsAndReceiversForSender(userId);

        for (Object[] data : chattersData) {
            Long listingId = (Long) data[0];
            Long receiverId = (Long) data[1];

            String chatKey = listingId + "_" + receiverId;
            if (!chatMap.containsKey(chatKey)) {
                Listing listing = listingRepository.findById(listingId)
                        .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
                User receiver = userRepository.findById(receiverId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

                ListingChatDTO chatDTO = createListingChatDTO(listing, receiver, false);
                updateLastMessageAndUnreadCount(chatDTO, listingId, userId, receiverId);
                chatMap.put(chatKey, chatDTO);
            }
        }

        return new ArrayList<>(chatMap.values());
    }

    private ListingChatDTO createListingChatDTO(Listing listing, User otherUser, boolean isUserSeller) {
        ListingChatDTO dto = new ListingChatDTO();
        dto.setListingId(listing.getId());
        dto.setListingTitle(listing.getTitle());
        dto.setListingOwnerId(listing.getUser().getId());
        dto.setListingOwnerName(listing.getUser().getUsername());
        dto.setListingImageUrl(listing.getImages().getFirst().getUrl());
        dto.setListingPrice(Double.valueOf(listing.getPrice()));
        dto.setUserSeller(isUserSeller);
        dto.setOtherUserId(otherUser.getId());
        dto.setOtherUserName(otherUser.getUsername());
        return dto;
    }

    private void updateLastMessageAndUnreadCount(ListingChatDTO dto, Long listingId, Long userId, Long otherId) {
        ChatMessage lastMessage = chatMessageRepository
                .findFirstByListingIdAndParticipantsOrderByTimestampDesc(
                        listingId, userId, otherId);

        if (lastMessage != null) {
            dto.setLastMessageTime(lastMessage.getTimestamp());
            dto.setLastMessageContent(lastMessage.getContent());

            int unreadCount = chatMessageRepository
                    .countUnreadMessages(listingId, userId, otherId);
            dto.setUnreadCount(unreadCount);
        }
    }

    public List<ChatMessageDTO> getMessagesByListing(Long listingId) {
        List<ChatMessage> messages = chatMessageRepository.findByListingIdOrderByTimestampDesc(listingId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private ChatMessageDTO convertToDTO(ChatMessage chatMessage) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(chatMessage.getId());
        dto.setSenderId(chatMessage.getSenderId());
        dto.setReceiverId(chatMessage.getReceiverId());
        dto.setContent(chatMessage.getContent());
        dto.setTimestamp(chatMessage.getTimestamp());
        dto.setListingId(chatMessage.getListingId());
        dto.setRead(chatMessage.isRead());
        dto.setFileUrls(chatMessage.getFileUrls()); // Gắn fileUrls vào DTO
        return dto;
    }

}