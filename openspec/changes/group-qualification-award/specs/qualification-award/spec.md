## ADDED Requirements

### Requirement: Qualified prize level
The system SHALL extend `PrizeLevel` enum to include `QUALIFIED(4, "合格奖")`. The `PrizeEntity` SHALL support creating and displaying prizes with level=4, bound to specific groups via `boundGroupIds`.

#### Scenario: Create qualified prize
- **WHEN** a prize is created with level=4 (QUALIFIED)
- **THEN** the prize SHALL be stored with level=4 and display name "合格奖"

#### Scenario: Bind qualified prize to group
- **WHEN** a qualified prize is bound to group IDs [1, 2]
- **THEN** the prize's `boundGroupIds` SHALL contain [1, 2]

### Requirement: Qualified award state in RewardUiState
The system SHALL add a `ShowQualified` state to `RewardUiState` containing: `groupName` (String), `qualificationDuration` (Long), `currentDuration` (Long), and `rewardInfo` (RewardInfo?). The system SHALL also extend the existing `ShowEncouragement` state to include optional `qualificationDuration` (Long?) and `currentDuration` (Long?) fields, enabling the encouragement dialog to display qualification line comparison when applicable.

#### Scenario: ShowQualified state is created
- **WHEN** a timer finishes with rank > 3 and duration <= qualificationDuration
- **THEN** the `rewardUiState` SHALL be set to `ShowQualified` with the group name, qualification duration, current duration, and matching prize info

#### Scenario: ShowEncouragement state with qualification info
- **WHEN** a timer finishes with duration > qualificationDuration (regardless of rank)
- **THEN** the `rewardUiState` SHALL be set to `ShowEncouragement` with the group name, qualificationDuration, and currentDuration

#### Scenario: ShowEncouragement state without qualification info
- **WHEN** a timer finishes with rank > 3 and no qualification line is set
- **THEN** the `rewardUiState` SHALL be set to `ShowEncouragement` with the group name only (qualificationDuration and currentDuration are null)

### Requirement: Qualification award determination logic
The system SHALL modify the `calculateRanking` method to check qualification **before** ranking determination. The priority order is: (1) if the group has a `qualificationDuration` set and `duration > qualificationDuration`, show encouragement dialog regardless of rank; (2) if rank <= 3 and duration does not exceed qualification line (or no qualification line set), show ranking dialog; (3) if rank > 3, qualification line is set, and `duration <= qualificationDuration`, show qualified award dialog; (4) if rank > 3 and no qualification line is set, show encouragement dialog.

#### Scenario: Timer finishes with rank <= 3 but duration above qualification line
- **WHEN** a timer finishes with rank <= 3 for a group that has qualificationDuration = 60000, and the current duration is 90000 (1min 30sec)
- **THEN** the system SHALL set `rewardUiState` to `ShowEncouragement` with the group name, qualificationDuration=60000, and currentDuration=90000

#### Scenario: Timer finishes with rank <= 3 and duration below qualification line
- **WHEN** a timer finishes with rank <= 3 for a group that has qualificationDuration = 120000, and the current duration is 90000 (1min 30sec)
- **THEN** the system SHALL set `rewardUiState` to `ShowRanking` as before (qualification check does not override)

#### Scenario: Timer finishes with rank > 3 and duration below qualification line
- **WHEN** a timer finishes with rank > 3 for a group that has qualificationDuration = 120000, and the current duration is 90000 (1min 30sec)
- **THEN** the system SHALL set `rewardUiState` to `ShowQualified` with the group name, qualificationDuration=120000, currentDuration=90000, and matching qualified prize if available

#### Scenario: Timer finishes with rank > 3 and duration above qualification line
- **WHEN** a timer finishes with rank > 3 for a group that has qualificationDuration = 60000, and the current duration is 90000 (1min 30sec)
- **THEN** the system SHALL set `rewardUiState` to `ShowEncouragement` with the group name, qualificationDuration=60000, and currentDuration=90000

#### Scenario: Timer finishes with rank > 3 and no qualification line set
- **WHEN** a timer finishes with rank > 3 for a group that has qualificationDuration = null
- **THEN** the system SHALL set `rewardUiState` to `ShowEncouragement` with the group name (no qualification/current duration info)

#### Scenario: Timer finishes with rank <= 3 and no qualification line set
- **WHEN** a timer finishes with rank <= 3 for a group that has qualificationDuration = null
- **THEN** the system SHALL set `rewardUiState` to `ShowRanking` as before

#### Scenario: Qualified prize found for the group
- **WHEN** the system determines a qualified award and a prize with level=4 bound to the group exists
- **THEN** the `rewardInfo` in `ShowQualified` SHALL contain the prize name and image path

#### Scenario: No qualified prize found for the group
- **WHEN** the system determines a qualified award but no prize with level=4 bound to the group exists
- **THEN** the `rewardInfo` in `ShowQualified` SHALL be null

### Requirement: Qualified award dialog UI
The system SHALL display a qualified award dialog when `rewardUiState` is `ShowQualified`. The dialog SHALL show a checkmark icon with green theme, the text "达标！", the group name and current duration, a note showing the qualification line duration, and optionally the qualified prize image and name.

#### Scenario: Qualified award dialog displayed with prize
- **WHEN** `rewardUiState` is `ShowQualified` with groupName="跑步", currentDuration=90000, qualificationDuration=120000, and rewardInfo with prizeName="运动毛巾"
- **THEN** the dialog SHALL display a green checkmark icon, "达标！" title, "跑步 · 00:01:30" subtitle, "合格线 00:02:00" note, and the prize image and name "运动毛巾"

#### Scenario: Qualified award dialog displayed without prize
- **WHEN** `rewardUiState` is `ShowQualified` with rewardInfo = null
- **THEN** the dialog SHALL display the qualified award without the prize section

#### Scenario: Dismiss qualified award dialog
- **WHEN** user taps the "知道了" button on the qualified award dialog
- **THEN** the dialog SHALL be dismissed and `rewardUiState` SHALL be set to `Hidden`
