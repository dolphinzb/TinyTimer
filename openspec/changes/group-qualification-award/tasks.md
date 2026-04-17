## 1. 数据层：GroupEntity 扩展与数据库迁移

- [x] 1.1 在 `GroupEntity` 中新增 `qualificationDuration: Long? = null` 字段
- [x] 1.2 在 `AppDatabase` 中升级数据库版本号，添加自动迁移或手动迁移（ALTER TABLE groups ADD COLUMN qualificationDuration INTEGER DEFAULT NULL）
- [x] 1.3 更新 `GroupDao` 中相关查询方法（如有需要）
- [x] 1.4 更新 `GroupRepository` 中 `createGroup` 和 `updateGroup` 方法以支持 `qualificationDuration` 参数
- [x] 1.5 更新 `GroupViewModel` 中分组创建/编辑逻辑以传递 `qualificationDuration`

## 2. 数据层：PrizeLevel 枚举扩展

- [x] 2.1 在 `PrizeLevel` 枚举中新增 `QUALIFIED(4, "合格奖")`
- [x] 2.2 更新 `PrizeLevel.fromValue()` 方法以正确处理 level=4 的值

## 3. 模型层：RewardUiState 扩展

- [x] 3.1 在 `RewardUiState` sealed class 中新增 `ShowQualified(groupName: String, qualificationDuration: Long, currentDuration: Long, rewardInfo: RewardInfo?)` 状态
- [ ] 3.2 扩展 `ShowEncouragement` 状态，增加可选字段 `qualificationDuration: Long? = null` 和 `currentDuration: Long? = null`，用于在鼓励弹窗中显示合格线对比信息

## 4. 业务逻辑：排名计算修改

- [x] 4.1 修改 `TimerViewModel.calculateRanking()` 方法，将合格线检查提前至排名判断之前，新逻辑优先级为：
  1. 如果分组设置了 `qualificationDuration` 且 `duration > qualificationDuration`，不论排名如何均设置 `ShowEncouragement` 状态（携带 qualificationDuration 和 currentDuration）
  2. 如果排名前三（rank <= 3）且时长未超过合格线（或未设置合格线），设置 `ShowRanking` 状态
  3. 如果排名4+（rank > 3）且 `duration <= qualificationDuration`，查找合格奖奖品并设置 `ShowQualified` 状态
  4. 如果排名4+且未设置合格线，设置 `ShowEncouragement` 状态（无合格线信息）
- [x] 4.2 确保所有调用 `calculateRanking` 的路径（stopTimer、quickStop、confirmStop、stopGroup）均能正确触发合格线判断

## 5. UI：分组编辑页合格线输入

- [x] 5.1 在 `GroupPage` 的分组编辑弹窗（AlertDialog）中增加合格线时长输入控件，支持时:分:秒格式输入
- [x] 5.2 编辑弹窗打开时回显已有合格线时长（将毫秒转换为 HH:mm:ss 格式）
- [x] 5.3 保存时将输入的时长转换为毫秒并存入 `GroupEntity.qualificationDuration`，空输入存储为 null
- [x] 5.4 在 `GroupItem` 列表项中显示合格线时长标识（可选，如小标签）

## 6. UI：弹窗更新

- [x] 6.1 在 `RewardDialog.kt` 中新增 `QualifiedDialog` composable，使用绿色主题勾选图标、"达标！"标题、分组名和时长信息、合格线说明、可选奖品展示
- [x] 6.2 在 `RewardDialog` 的 when 分支中添加 `ShowQualified` 状态处理，调用 `QualifiedDialog`
- [x] 6.3 实现合格奖弹窗的"知道了"按钮关闭逻辑
- [ ] 6.4 更新 `EncouragementDialog`，当 `ShowEncouragement` 携带 `qualificationDuration` 和 `currentDuration` 时，显示合格线对比信息（如"合格线 XX:XX:XX，用时 XX:XX:XX"），支持 `formatTime` 参数
- [ ] 6.5 更新 `RewardDialog` 的 when 分支中 `ShowEncouragement` 的调用，传递 qualificationDuration、currentDuration 和 formatTime 参数

## 7. 验证与测试

- [ ] 7.1 验证数据库迁移：从旧版本升级后分组数据正常保留，qualificationDuration 默认为 null
- [ ] 7.2 验证合格线设置：创建/编辑分组时可正确设置和清除合格线时长
- [ ] 7.3 验证合格奖触发：rank > 3 且 duration <= qualificationDuration 时弹出合格奖弹窗
- [ ] 7.4 验证前三名超合格线：rank <= 3 但 duration > qualificationDuration 时弹出鼓励弹窗（含合格线对比信息）
- [ ] 7.5 验证前三名未超合格线：rank <= 3 且 duration <= qualificationDuration 时正常显示排名弹窗
- [ ] 7.6 验证排名4+超合格线：rank > 3 且 duration > qualificationDuration 时弹出鼓励弹窗（含合格线对比信息）
- [ ] 7.7 验证未设置合格线：rank > 3 且无合格线时弹出鼓励弹窗（无合格线对比信息）
- [ ] 7.8 验证未设置合格线：rank <= 3 且无合格线时正常显示排名弹窗
