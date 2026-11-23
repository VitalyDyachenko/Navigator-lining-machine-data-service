
package ru.ccfit.nsu.navigator.dataservice.entity.railway;

import jakarta.persistence.*;

@Entity
@Table(name = "railway")
public class Railway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private short code;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "short_name", length = 6)
    private String shortName;

    protected Railway() {
    }

    public Railway(short code, String name, String shortName) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
    }

    public Long getId() {
        return id;
    }
}
