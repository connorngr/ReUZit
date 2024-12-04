package com.connorng.ReUzit.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private List<String> fileUrls;
    private LocalDateTime timestamp;
    private Long listingId;
    private boolean isSellerMessage;
    private boolean isRead;
    private String chatKey;
    private String otherUserName;
}
