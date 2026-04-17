## Why

当前奖励系统仅支持前三名排名奖和排名4+的鼓励提示，缺少"合格线"概念。用户希望能为每个分组设定一个合格线时长，当计时时长低于合格线时给予"合格奖"肯定，而非仅显示"继续加油"的鼓励文案。同时，即使排名前三，如果时长超过合格线，也应弹出鼓励弹窗而非排名弹窗，以强调合格线的优先级。这样可以为所有计时提供基于合格线的正向/负向反馈，而不仅仅基于相对排名。

## What Changes

- 在 `GroupEntity` 中新增 `qualificationDuration` 字段（Long?，毫秒），用于存储每个分组的合格线时长，null 表示未设置
- 在分组设置页面（`GroupPage`）的编辑弹窗中增加合格线时长输入控件
- 在 `PrizeLevel` 枚举中新增 `QUALIFIED(4, "合格奖")` 级别，`PrizeEntity` 支持设置合格奖奖品
- 在 `RewardUiState` 中新增 `ShowQualified` 状态，包含分组名称、合格线时长、当次时长和奖品信息；扩展 `ShowEncouragement` 状态，增加合格线时长和当次时长字段（可选），用于在鼓励弹窗中显示合格线对比
- 修改 `calculateRanking` 逻辑：优先检查合格线——如果设置了合格线且计时时长超过合格线，不论排名如何均弹出鼓励弹窗；如果时长低于合格线且排名前三则弹出排名弹窗；如果时长低于合格线且排名4+则弹出合格奖弹窗；未设置合格线时保持原有逻辑
- 在 `RewardDialog` 中新增合格奖弹窗 UI，展示合格奖图标、达标信息和奖品；更新鼓励弹窗，当有时长和合格线信息时显示对比

## Capabilities

### New Capabilities
- `group-qualification`: 分组合格线设置能力，包括 GroupEntity 新增字段、数据库迁移、分组编辑页合格线输入
- `qualification-award`: 合格奖奖励能力，包括 PrizeLevel 扩展、合格奖弹窗 UI、排名计算逻辑修改

### Modified Capabilities

## Impact

- **数据库**: `GroupEntity` 新增 `qualificationDuration` 字段，需要 Room 数据库迁移（版本升级）
- **数据层**: `GroupDao`、`GroupRepository` 需要适配新字段
- **UI 层**: `GroupPage` 编辑弹窗增加合格线输入控件；`RewardDialog` 新增合格奖弹窗样式
- **ViewModel**: `TimerViewModel.calculateRanking()` 逻辑修改，增加合格线判断分支
- **模型层**: `PrizeLevel` 枚举扩展、`RewardUiState` 新增状态
