# manual-record-entry

## ADDED Requirements

### Requirement: 手动添加计时记录

系统 SHALL 提供手动添加计时记录的功能，允许用户输入历史计时数据并保存到数据库。

#### Scenario: 成功添加手动记录

- **WHEN** 用户在历史记录页面点击添加按钮
- **THEN** 系统显示手动添加记录对话框

#### Scenario: 填写并保存有效记录

- **WHEN** 用户填写有效的开始时间、时长，选择分组后点击保存
- **THEN** 系统验证数据有效后，将记录保存到数据库
- **AND** 对话框关闭
- **AND** 历史记录列表刷新显示新记录

#### Scenario: 开始时间验证

- **WHEN** 用户选择的开始时间晚于当前时间
- **THEN** 系统显示错误提示，阻止保存

#### Scenario: 时长验证

- **WHEN** 用户输入的时长小于等于零
- **THEN** 系统显示错误提示，阻止保存

#### Scenario: 取消添加

- **WHEN** 用户点击取消或对话框外区域
- **THEN** 对话框关闭，不保存任何数据

#### Scenario: 无分组选择

- **WHEN** 用户不选择任何分组（保持为空）
- **THEN** 系统将 groupId 设置为 null，记录为未分组

### Requirement: 时间输入方式

系统 SHALL 提供直观的时间输入界面。

#### Scenario: 日期选择

- **WHEN** 用户点击日期字段
- **THEN** 系统显示 DatePicker 供用户选择日期

#### Scenario: 时间选择

- **WHEN** 用户点击时间字段
- **THEN** 系统显示 TimePicker 供用户选择小时和分钟

#### Scenario: 时长输入

- **WHEN** 用户在时长字段输入数值
- **THEN** 系统验证输入为非负整数

### Requirement: 分组选择

系统 SHALL 允许用户为手动记录选择关联分组。

#### Scenario: 从下拉列表选择分组

- **WHEN** 用户点击分组选择字段
- **THEN** 系统显示分组下拉列表，包含所有现有分组和"无分组"选项

#### Scenario: 确认分组选择

- **WHEN** 用户从列表中选择某个分组
- **THEN** 下拉列表关闭，选中的分组名称显示在字段中