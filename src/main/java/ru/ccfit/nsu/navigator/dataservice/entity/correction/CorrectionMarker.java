package ru.ccfit.nsu.navigator.dataservice.entity.correction;

import jakarta.persistence.*;

@Entity
@Table(name = "correction_marker")
public class CorrectionMarker {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fragment_id", nullable = false)
	private Long fragmentId;

	@Column(name = "type_id", nullable = false)
	private short typeId;

	@Column(name = "step_index", nullable = false)
	private long stepIndex;

	protected CorrectionMarker() {
	}

	public CorrectionMarker(Long fragmentId, short typeId, long stepIndex) {
		this.fragmentId = fragmentId;
		this.typeId = typeId;
		this.stepIndex = stepIndex;
	}
}