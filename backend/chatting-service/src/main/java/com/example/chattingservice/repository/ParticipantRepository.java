package com.example.chattingservice.repository;

import com.example.chattingservice.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByUserIdAndChannelId(Long userId, Long channelId);
}