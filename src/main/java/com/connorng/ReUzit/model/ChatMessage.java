package com.connorng.ReUzit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime timestamp;
    private Long listingId;
    private boolean isRead;
    @ElementCollection
    @CollectionTable(name = "file_urls", joinColumns = @JoinColumn(name = "chat_message_id"))
    @Column(name = "file_url")
    private List<String> fileUrls = new ArrayList<>();
}