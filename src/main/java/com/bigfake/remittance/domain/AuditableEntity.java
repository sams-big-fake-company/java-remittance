package com.bigfake.remittance.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter
@MappedSuperclass
public abstract class AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    @Type(type = "yes_no")
    @Column(name = "is_enabled", columnDefinition = "CHAR(1)")
    private Boolean enabled = Boolean.TRUE;
    @PrePersist public void beforeInsert() {
        createdAt = LocalDateTime.now(); updatedAt = createdAt;
        if (createdBy == null) createdBy = "system";
    }
    @PreUpdate public void beforeUpdate() { updatedAt = LocalDateTime.now(); updatedBy = "system"; }
}
