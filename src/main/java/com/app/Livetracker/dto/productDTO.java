package com.app.Livetracker.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class productDTO {
    private Long pid;
    private String pname;
    private String description;
    private Double price;
    private Integer stock;
    private String photo;

}
