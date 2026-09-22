package com.bluewave.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Address implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String city;
    private String state;
    private int postcode;
    private String address;

}
