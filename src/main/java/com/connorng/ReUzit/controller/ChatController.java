package com.connorng.ReUzit.controller;

import com.connorng.ReUzit.dto.ChatMessageDTO;
import com.connorng.ReUzit.dto.ListingChatDTO;
import com.connorng.ReUzit.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> fileUrls = chatService.uploadFiles(files);
        return ResponseEntity.ok(fileUrls);
    }

    @MessageMapping("/send")
    @SendTo("/topic/messages")
    public ChatMessageDTO processMessage(@Payload ChatMessageDTO chatMessageDTO) throws IOException {
        return chatService.saveMessage(chatMessageDTO);
    }

    @Cacheable("chatMessages")
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam Long listingId,
            @RequestParam(required = false) Long before,
            @RequestParam(defaultValue = "20") int limit) {
        List<ChatMessageDTO> messages = chatService.getMessages(senderId, receiverId, listingId, before, limit);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesByListing(@PathVariable Long listingId) {
        List<ChatMessageDTO> messages = chatService.getMessagesByListing(listingId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/user-chats")
    public ResponseEntity<List<ListingChatDTO>> getUserChats(@RequestParam Long userId) {
        List<ListingChatDTO> chats = chatService.getAllUserChats(userId);
        return ResponseEntity.ok(chats);
    }

}