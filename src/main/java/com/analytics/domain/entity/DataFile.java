package com.analytics.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DataFile Entity
 * Represents uploaded data files (Excel, CSV, etc.)
 */
@Entity
@Table(name = "data_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataFile extends BaseEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank
    @Size(max = 500)
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @NotBlank
    @Size(max = 50)
    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType; // EXCEL, CSV, JSON, SPSS

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "column_count")
    private Integer columnCount;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull
    private User owner;

    @Column(name = "is_processed")
    private boolean processed = false;

    @Lob
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata about the file

    /**
     * Get file extension
     */
    public String getFileExtension() {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }
}
