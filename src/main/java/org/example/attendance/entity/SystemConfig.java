package org.example.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SystemConfig extends BaseEntity{
    @Column(name = "config_key", nullable = false, unique = true)
    private String key;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(name = "description")
    private String description;

    @Column(name = "config_type")
    private String type; // STRING, INTEGER, JSON, BOOLEAN, TIME

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}
