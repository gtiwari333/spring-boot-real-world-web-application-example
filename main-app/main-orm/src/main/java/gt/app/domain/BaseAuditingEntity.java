package gt.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditingEntity extends BaseEntity {

    private static final long serialVersionUID = 4681401402666658611L;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", updatable = false)
    @JsonIgnore//ignore completely to avoid StackOverflow exception by User.createdByUser logic, use DTO
    private AppUser createdByUser;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_user_id")
    @JsonIgnore//ignore completely to avoid StackOverflow exception by User.lastModifiedByUser logic, use DTO
    private AppUser lastModifiedByUser;

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;
}
