package com.exadel.frs.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"app", "requestId"})
public class ModelShareRequest {

    @EmbeddedId
    private ModelShareRequestId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("appId")
    private App app;

    @CreationTimestamp
    private LocalDateTime requestTime;
}