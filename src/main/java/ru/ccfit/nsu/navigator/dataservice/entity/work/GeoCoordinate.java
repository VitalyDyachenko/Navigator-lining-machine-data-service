
package ru.ccfit.nsu.navigator.dataservice.entity.work;

import jakarta.persistence.*;

@Entity
@Table(name = "geo_coord")
public class GeoCoordinate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "point_index", nullable = false)
    private int pointIndex;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "altitude", nullable = false)
    private double altitude;

    @Column(name = "is_project", nullable = false)
    private boolean isProject;

    protected GeoCoordinate() {
    }

    public GeoCoordinate(Long workId, boolean isProject, int pointIndex, double latitude,
                         double longitude, double altitude) {
        this.workId = workId;
        this.pointIndex = pointIndex;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.isProject = isProject;
    }
}
