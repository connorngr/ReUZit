package com.connorng.ReUzit.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListingChatDTO {
    private Long listingId;
    private String listingTitle;
    private Long listingOwnerId;
    private String listingOwnerName;
    private String listingImageUrl;
    private Double listingPrice;
    private List<ChatMessageDTO> messages;
    private boolean isUserSeller;
    private Long otherUserId;
    private String otherUserName;
    private LocalDateTime lastMessageTime;
    private String lastMessageContent;
    private int unreadCount;
}