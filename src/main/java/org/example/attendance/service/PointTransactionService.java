package org.example.attendance.service;

import org.example.attendance.dto.PagingResponse;
import org.example.attendance.dto.res.PointTransactionResponse;
import org.example.attendance.entity.PointTransaction;
import org.example.attendance.entity.User;
import org.example.attendance.repository.PointTransactionRepository;
import org.example.attendance.repository.UserRepository;
import org.example.attendance.utils.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointTransactionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Transactional
    public PointTransaction deductPoints(String userId, Long points, String description, String referenceId) {
        if (points <= 0) {
            throw new RuntimeException("Points must be positive");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getLotusPoints() < points) {
            throw new RuntimeException("Insufficient points");
        }

        user.setLotusPoints(user.getLotusPoints() - points);
        userRepository.save(user);

        PointTransaction transaction = new PointTransaction();
        transaction.setUser(user);
        transaction.setTransactionType(PointTransaction.TransactionType.DEDUCTION);
        transaction.setPoints(-points);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);

        return pointTransactionRepository.save(transaction);
    }

    public PagingResponse<List<PointTransactionResponse>> getPointHistory(String userId, int page, int size) {
        Pageable pageable =  PageRequest.of(page, size);


        Page<PointTransaction> recordsPage;

        recordsPage = pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<PointTransactionResponse> recordDTOs = recordsPage.getContent().stream()
                .map(this::convertToPointTransactionResponse)
                .collect(Collectors.toList());

        PagingResponse<List<PointTransactionResponse>> response = PagingResponse.of(
                recordDTOs,
                page,
                size,
                recordsPage.getTotalElements(),
                recordsPage.getTotalPages()
        );


        return response;
    }

    public Long getUserTotalPoints(String userId) {
        return userRepository.findLotusPointsByUserId(userId).orElse(0L);
    }

    private PointTransactionResponse convertToPointTransactionResponse(PointTransaction record) {

        PointTransactionResponse dto =
                MapperUtils.toDTO(record,PointTransactionResponse.class);


        if (record.getCreatedAt() != null) {
            dto.setCreatedAt(java.time.Instant.ofEpochMilli(record.getCreatedAt())
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return dto;
    }
}