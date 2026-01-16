package com.app.Livetracker.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "product")
public class products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;
    private String pname;
    private String description;
    private Double price;
    private Integer stock;
    private String photo;
}

