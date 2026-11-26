package ru.ccfit.nsu.navigator.dataservice.parser.crm.model;

import java.time.LocalTime;

// Модель цикла подбивки tsPRU_CYCLE
public record CrmPruCycle(
		int posStepTenths,
		LocalTime recordedTime,
		int cycleDurationTenthSec,
		int cycleNumber,
		boolean rightPress,
		boolean alignmentEnabled,
		boolean leftLiftEnabled,
		boolean rightLiftEnabled,
		short deltaAlignment,
		short deltaLeftLift,
		short deltaRightLift,
		boolean alertRegim,
		boolean alertSkipTrack,
		boolean alertRollback
) {}