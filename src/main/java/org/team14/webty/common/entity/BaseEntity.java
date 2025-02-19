package org.team14.webty.common.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity { // Review.kt에서 접근하기 위해 public으로 변경
	@CreatedDate
	@Setter(AccessLevel.PRIVATE)
	public LocalDateTime createdAt;

	@LastModifiedDate
	@Column(insertable = false)  // 업데이트할 때만 변경
	@Setter(AccessLevel.PRIVATE)
	public LocalDateTime modifiedAt;
}
