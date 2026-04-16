## 1. 奖励配置与数据模型

- [x] 1.1 创建 `TimerRanking.kt` 数据类，包含 groupId、groupName、currentDuration、rank
- [x] 1.2 创建 `RewardInfo.kt` 数据类，包含 rank、prizeName、prizeImagePath

## 2. 数据层扩展

- [x] 2.1 在 `RecordDao` 添加 `countRecordsShorterThan(groupId: Long, duration: Long)` 方法，统计某分组内比指定时长更短的记录数
- [x] 2.2 在 `RecordRepository` 暴露上述方法
- [x] 2.3 在 `PrizeDao` 添加 `getPrizesByGroupIdOnce(groupId: Long)` suspend 方法，并在 `PrizeRepository` 暴露

## 3. 排名计算逻辑

- [x] 3.1 在 `TimerViewModel` 添加 `calculateRanking(currentDuration: Long, groupId: Long)` 方法
- [x] 3.2 调用 `countRecordsShorterThan(groupId, duration)` 获取比当次更短的记录数，计算排名（排名 = 更短记录数 + 1）
- [x] 3.3 添加 `UiState` 状态：`ShowRanking(ranking: TimerRanking)` 和 `ShowEncouragement`
- [x] 3.4 跟踪所有运行中的分组（可能有多个同时计时），仅当所有分组都停止时才触发排名展示

## 4. 奖励弹窗 UI

- [x] 4.1 创建 `RewardDialog.kt` Composable 组件
- [x] 4.2 实现半透明遮罩背景
- [x] 4.3 实现排名结果展示（显示"第X名"、分组名、时长、排名图标）
- [x] 4.4 实现奖品名称和图标展示
- [x] 4.5 实现"继续加油"鼓励弹窗（排名4+）
- [x] 4.6 实现弹窗关闭功能（点击外部/关闭按钮）

## 5. 庆祝动效

- [x] 5.1 创建 `FireworkAnimation.kt` Composable 组件，使用 Canvas 实现烟花粒子动画
- [x] 5.2 创建 `FlowerAnimation.kt` Composable 组件，使用 Canvas 实现鲜花粒子动画
- [x] 5.3 在 `RewardDialog` 中集成动效：rank 1 显示烟花，rank 2-3 显示鲜花
- [x] 5.4 确保粒子数量不超过 50 个以保证性能

## 6. UI 集成

- [x] 6.1 在 `TimerPage` 添加 `RewardDialog` 组件
- [x] 6.2 在计时停止时触发排名计算和弹窗显示
- [x] 6.3 添加默认占位图标 `ic_reward_placeholder.xml` drawable 资源

## 7. 测试验证

- [x] 7.1 验证分组内有 0 条记录时（首次计时）排名 = 1
- [x] 7.2 验证分组内有多条记录时正确计算排名
- [x] 7.3 验证排名前三显示烟花/鲜花动效
- [x] 7.4 验证排名4+显示鼓励提示
- [x] 7.5 验证奖品图标加载（成功/失败情况）
- [x] 7.6 验证弹窗关闭功能
- [x] 7.7 验证多分组同时计时场景
