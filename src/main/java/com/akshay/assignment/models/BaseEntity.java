package com.akshay.assignment.models;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity // To mark this as a base class for other entities
public class BaseEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id

    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

//    @Version
//    private int version;

}
