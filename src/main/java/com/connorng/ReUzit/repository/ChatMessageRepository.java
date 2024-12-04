package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByListingIdOrderByTimestampDesc(Long listingId);
    @Query("SELECT c FROM ChatMessage c WHERE c.listingId = :listingId " +
            "AND ((c.senderId = :user1Id AND c.receiverId = :user2Id) " +
            "OR (c.senderId = :user2Id AND c.receiverId = :user1Id)) " +
            "AND c.id < :beforeId ORDER BY c.timestamp DESC")
    List<ChatMessage> findByListingIdAndParticipants(
            Long listingId, Long user1Id, Long user2Id, Long beforeId, Pageable pageable);

    @Query("SELECT c FROM ChatMessage c WHERE c.listingId = :listingId " +
            "AND ((c.senderId = :user1Id AND c.receiverId = :user2Id) " +
            "OR (c.senderId = :user2Id AND c.receiverId = :user1Id)) " +
            "ORDER BY c.timestamp DESC")
    List<ChatMessage> findByListingIdAndParticipants(
            Long listingId, Long user1Id, Long user2Id, Pageable pageable);

    @Query("SELECT DISTINCT c.senderId FROM ChatMessage c " +
            "WHERE c.listingId = :listingId AND c.receiverId = :receiverId")
    List<Long> findDistinctSendersByListingAndReceiver(Long listingId, Long receiverId);

    @Query("SELECT DISTINCT c.listingId, c.receiverId FROM ChatMessage c " +
            "WHERE c.senderId = :senderId")
    List<Object[]> findDistinctListingsAndReceiversForSender(Long senderId);

    @Query("SELECT c FROM ChatMessage c WHERE c.listingId = :listingId " +
            "AND ((c.senderId = :user1Id AND c.receiverId = :user2Id) " +
            "OR (c.senderId = :user2Id AND c.receiverId = :user1Id)) " +
            "ORDER BY c.timestamp DESC LIMIT 1")
    ChatMessage findFirstByListingIdAndParticipantsOrderByTimestampDesc(
            Long listingId, Long user1Id, Long user2Id);

    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.listingId = :listingId " +
            "AND c.senderId = :senderId AND c.receiverId = :receiverId " +
            "AND c.isRead = false")
    int countUnreadMessages(Long listingId, Long receiverId, Long senderId);
}
