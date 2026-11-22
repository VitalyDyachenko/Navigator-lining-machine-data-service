
package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "machine")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number")
    private Long number;

    @Column(name = "name")
    private String name;

    @Column(name = "type_id")
    private Short typeId;

    protected Machine() {
    }

    public Machine(long number, String name, short typeId) {
        this.number = number;
        this.name = name;
        this.typeId = typeId;
    }

    public Long getId() {
        return id;
    }
}
