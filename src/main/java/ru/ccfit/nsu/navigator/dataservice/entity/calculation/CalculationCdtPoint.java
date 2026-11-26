package ru.ccfit.nsu.navigator.dataservice.entity.calculation;

import jakarta.persistence.*;

@Entity
@Table(name = "calculation_cdt_point")
public class CalculationCdtPoint {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "work_id", nullable = false)
	private Long workId;

	@Column(name = "step_index", nullable = false)
	private int stepIndex;

	@Column(name = "is_project_axis", nullable = false)
	private boolean projectAxis;

	@Column(name = "x", nullable = false)
	private double x;

	@Column(name = "y", nullable = false)
	private double y;

	@Column(name = "h", nullable = false)
	private double h;

	protected CalculationCdtPoint() {
	}

	public CalculationCdtPoint(
			Long workId,
			int stepIndex,
			boolean projectAxis,
			double x,
			double y,
			double h
	) {
		this.workId = workId;
		this.stepIndex = stepIndex;
		this.projectAxis = projectAxis;
		this.x = x;
		this.y = y;
		this.h = h;
	}

	public int getStepIndex() {
		return stepIndex;
	}

	public boolean isProjectAxis() {
		return projectAxis;
	}
}