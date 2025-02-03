package com.example.peollys3.entities;

import com.example.peollys3.enums.FileExtension;
import com.example.peollys3.enums.FileStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file_links")
public class FileLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String fileUrl;

    @Column
    private String fileName;

    @Column
    private String bucketName;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private LocalDateTime uploadDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column
    private FileStatus status;

    @Enumerated(EnumType.STRING)
    @Column
    private FileExtension extension;
}
