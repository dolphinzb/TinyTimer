## ADDED Requirements

### Requirement: Group qualification duration field
The system SHALL add a `qualificationDuration` field (Long?, milliseconds) to `GroupEntity`. When null, the group has no qualification line set. When set to a positive value, it represents the maximum duration (in ms) that qualifies as "passed" for that group.

#### Scenario: Create group without qualification duration
- **WHEN** a new group is created without specifying qualification duration
- **THEN** the `qualificationDuration` field SHALL be null

#### Scenario: Create group with qualification duration
- **WHEN** a new group is created with qualification duration set to 60000 (1 minute)
- **THEN** the `qualificationDuration` field SHALL be 60000

#### Scenario: Update group qualification duration
- **WHEN** an existing group's qualification duration is updated from null to 120000
- **THEN** the `qualificationDuration` field SHALL be 120000

#### Scenario: Clear group qualification duration
- **WHEN** an existing group's qualification duration is cleared (set to null)
- **THEN** the `qualificationDuration` field SHALL be null

### Requirement: Qualification duration input in group edit dialog
The system SHALL provide a qualification duration input field in the group edit dialog on `GroupPage`. The input SHALL allow users to set hours, minutes, and seconds for the qualification line. The input SHALL be optional — users can leave it empty to indicate no qualification line.

#### Scenario: Open group edit dialog with no qualification duration set
- **WHEN** user opens the edit dialog for a group with no qualification duration
- **THEN** the qualification duration input SHALL show empty/placeholder state

#### Scenario: Open group edit dialog with existing qualification duration
- **WHEN** user opens the edit dialog for a group with qualificationDuration = 90000 (1min 30sec)
- **THEN** the qualification duration input SHALL display "00:01:30"

#### Scenario: Set qualification duration via input
- **WHEN** user enters "00:02:00" in the qualification duration input and saves
- **THEN** the group's qualificationDuration SHALL be saved as 120000

#### Scenario: Clear qualification duration via input
- **WHEN** user clears the qualification duration input and saves
- **THEN** the group's qualificationDuration SHALL be saved as null

### Requirement: Database migration for qualification duration
The system SHALL perform a Room database migration to add the `qualificationDuration` column (INTEGER, nullable, default NULL) to the `groups` table.

#### Scenario: Database migration executes successfully
- **WHEN** the app is updated with the new GroupEntity schema
- **THEN** the `groups` table SHALL have a new `qualificationDuration` column with type INTEGER and default value NULL
- **AND** existing group data SHALL be preserved without modification
