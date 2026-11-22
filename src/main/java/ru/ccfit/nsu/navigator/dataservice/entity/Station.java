
package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "station")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_id", nullable = false)
    private int sourceId;

    @Column(name = "code")
    private int code;

    @Column(name = "department_id")
    private int departmentId;

    @Column(name = "okato_id")
    private int okatoId;

    @Column(name = "type_id")
    private short typeId;

    @Column(name = "name", length = 38)
    private String name;

    protected Station() {
    }

    public Station(int sourceId, int code, int departmentId,
                   int okatoId, short typeId, String name) {
        this.sourceId = sourceId;
        this.code = code;
        this.departmentId = departmentId;
        this.okatoId = okatoId;
        this.typeId = typeId;
        this.name = name;
    }

    public boolean hasSameData(Station other) {
        return code == other.code
                && departmentId == other.departmentId
                && okatoId == other.okatoId
                && typeId == other.typeId
                && Objects.equals(name, other.name);
    }

    public Long getId() {
        return id;
    }
}
