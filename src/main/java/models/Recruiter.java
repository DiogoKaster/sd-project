package models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "recruiters")
public class Recruiter {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "industry", nullable = false)
    private String industry;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(mappedBy = "recruiter")
    private Set<Job> jobs = new HashSet<>();
}
