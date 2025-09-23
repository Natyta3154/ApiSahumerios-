package com.example.AppSaumerios.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class RoleEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
}

