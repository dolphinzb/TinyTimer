## Context

TinyTimer 当前奖励系统基于分组内相对排名：时长越短排名越高，前三名（rank <= 3）显示排名弹窗并匹配对应奖品，排名 4+ 显示鼓励弹窗。GroupEntity 仅有 `id`、`name`、`color`、`createdAt` 四个字段，不存在合格线概念。PrizeLevel 枚举仅支持 FIRST/SECOND/THIRD 三个等级。Room 数据库使用自动迁移（`fallbackToDestructiveMigration` 尚未确认，需检查）。

## Goals / Non-Goals

**Goals:**
- 为每个分组支持设置可选的合格线时长（毫秒级精度）
- 在分组编辑页面提供合格线时长输入
- 扩展奖品等级，支持合格奖（level=4）
- 修改排名计算逻辑：排名 > 3 且时长低于合格线时弹出合格奖，时长高于合格线时保持鼓励弹窗
- 合格奖弹窗 UI 展示达标信息和可选奖品

**Non-Goals:**
- 不改变现有的排名计算方式（仍基于相对排名，时长越短越好）
- 不支持多级合格线（如优良中差等）
- 不修改 RecordEntity 或 TimerStateEntity 的数据结构
- 不实现合格线的批量设置功能

## Decisions

### 1. GroupEntity 新增字段方案

**决策**: 在 GroupEntity 中新增 `qualificationDuration: Long?` 字段，默认值为 `null`。

**理由**: 使用可空类型表示"未设置合格线"，避免用 0 或 -1 等魔法值。合格线时长以毫秒为单位存储，与现有 duration 字段保持一致。

**替代方案**: 创建独立的 GroupSettings 表——过度设计，合格线目前只有一个字段，不值得单独建表。

### 2. 数据库迁移策略

**决策**: 新增字段使用 Room 的自动迁移（AutoMigration），因为只是新增一个可空字段，不涉及数据转换。

**理由**: Room 2.4+ 支持 `@AutoMigration`，新增可空字段无需手动编写 SQL。如果项目未使用 AutoMigration，则使用手动迁移 `ALTER TABLE groups ADD COLUMN qualificationDuration INTEGER DEFAULT NULL`。

### 3. PrizeLevel 枚举扩展

**决策**: 新增 `QUALIFIED(4, "合格奖")` 到 PrizeLevel 枚举。

**理由**: level=4 紧跟在三等奖(level=3)之后，保持编号连续性。合格奖的奖品绑定机制与现有等级一致，复用 PrizeEntity 和 boundGroupIds 字段。

**替代方案**: 使用独立的 QualifiedPrize 表——不符合现有架构模式，增加不必要的复杂度。

### 4. RewardUiState 扩展方案

**决策**: 新增 `ShowQualified(groupName, qualificationDuration, currentDuration, rewardInfo)` 状态。

**理由**: 合格奖与前三名排名奖的展示内容不同（显示"达标"而非"第N名"），与 ShowEncouragement 也不同（需要展示奖品信息），因此需要独立的状态类型。

### 5. 合格线判断逻辑

**决策**: 在 `calculateRanking` 方法中，**优先检查合格线**：如果分组设置了 `qualificationDuration` 且 `duration > qualificationDuration`，则弹出鼓励弹窗（不论排名）；如果时长未超过合格线（或未设置合格线），则按排名判断：前三名显示排名弹窗，排名4+且时长达标显示合格奖弹窗，排名4+且无合格线显示鼓励弹窗。

**理由**: 
- 合格线是绝对标准（用时上限），排名是相对标准，绝对标准应优先于相对标准
- 即使排名前三，但时长超过合格线意味着"不够好"，应给予鼓励而非奖励
- "时长低于合格线"意味着用的时间更短（越好），即 `duration <= qualificationDuration`
- 合格线为 null 时不触发，保持向后兼容
- 优先级：时长超合格线（鼓励） > 前三名（排名奖） > 排名4+且达标（合格奖） > 排名4+且无合格线（鼓励）

**注意**: 当前排名逻辑是"时长越短排名越高"，所以"低于合格线"即 `duration <= qualificationDuration`，表示用时少于合格线时长。

### 6. 合格奖弹窗 UI

**决策**: 参考前三名弹窗风格，使用绿色主题（合格/通过语义），显示勾选图标、分组名、时长信息，以及可选的奖品信息。不使用烟花/鲜花特效，保持简洁的正向反馈。

## Risks / Trade-offs

- **[数据库迁移风险]** → 使用 ALTER TABLE ADD COLUMN 添加可空字段，风险极低。迁移前需确认当前数据库版本号。
- **[合格线语义混淆]** → "低于合格线"在时长语境下意为"用时更短"，需在 UI 中用清晰的文案表达（如"达标！用时 XX:XX，低于合格线 XX:XX"）。
- **[PrizeLevel 枚举扩展兼容性]** → 新增 QUALIFIED(4) 不影响现有数据，fromValue 方法默认回退到 FIRST，需更新为合理处理未知值。
- **[合格线为 0 的边界情况]** → qualificationDuration 为 0 时视为"未设置"还是"0毫秒合格线"？决策：仅 null 表示未设置，0 理论上不应被设置（无意义），UI 层做输入校验。
