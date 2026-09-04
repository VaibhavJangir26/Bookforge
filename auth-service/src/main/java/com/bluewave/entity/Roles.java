package com.bluewave.entity;

import com.bluewave.constants.AppRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = {"users"})
@EqualsAndHashCode(exclude = {"users"})
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private AppRole appRole;

    @ManyToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private Set<Users> users=new HashSet<>();
}
