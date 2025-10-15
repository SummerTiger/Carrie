package com.vending.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "procurement_receipt_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProcurementBatch batch;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "image_name", nullable = false)
    private String imageName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @CreationTimestamp
    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @Column(name = "image_order", nullable = false)
    @Builder.Default
    private Integer imageOrder = 0;
}
