## Context

TinyTimer 是一款计时器 Android 应用，用户可以为不同活动创建分组并计时。现有功能包括计时、分组管理、历史记录查看，但缺少激励机制。

**当前状态**:
- 用户在分组上计时，计时结束时记录存入 `records` 表
- 分组有 `GroupEntity(id, name, color)`，记录有 `RecordEntity(id, groupId, duration)`
- 历史排名仅支持按单条记录时长排序，不支持分组内排名

**需求**: 计时结束时，在当前分组内计算当次计时时长的排名，前三名展示庆祝动效和奖品。

## Goals / Non-Goals

**Goals:**
- 计时结束时自动触发分组内排名计算
- 在当前分组内按单次计时时长排名，展示第1-3名
- 前三名显示对应庆祝动效（烟火/鲜花）
- 奖品信息以弹窗形式展示分组+名次对应的奖品

**Non-Goals:**
- 不改变现有计时核心逻辑（TimerService）
- 不实现分组之间时长的排名比较
- 不实现积分/等级系统
- 不使用第三方后端，所有计算在本地完成

## Decisions

### 1. 排名计算方式

**决定**: 在**当前分组内**计算排名。排名 = 统计该分组内有多少条记录比当次计时更短 + 1。**时长越短排名越高**（即用时越少越厉害）。

例如：分组内有 5 条记录，时长分别为 [15min, 30min, 1h, 1.5h, 2h]
- 当次计时 1h → 比它更短的有 2 条 (15min, 30min) → 排名 = 3（前三！🌸）
- 当次计时 30min → 比它更短的有 1 条 (15min) → 排名 = 2（并列🌸）
- 当次计时 15min → 比它更短的有 0 条 → 排名 = 1（最短！🔥）

**并列处理**：相同时长的记录获得相同排名，新计时与已有相同时长记录并列。

**备选方案**:
- 时长越长排名越高 → 不合理，用户希望快速完成计时
- 分组间排名 → 用户理解成本高，且不公平（新分组永远垫底）

### 2. 奖励配置存储

**决定**: 复用现有的 `PrizeEntity` + `PrizeDao` 奖品系统，不新增配置文件。

- `PrizeEntity` 已有 `level`（1/2/3等奖）和 `boundGroupIds`（绑定分组）字段
- 通过 `PrizeDao.getPrizesByGroupId(groupId)` 查询某分组绑定的奖品
- 奖品图片通过 `imagePath` 字段指定的本地路径加载

**备选方案**:
- 新建 rewards.json → 增加冗余，现有 PrizeEntity 已足够
- 代码硬编码 → 不灵活，用户无法管理奖品

### 3. 动效实现

**决定**: 使用 Jetpack Compose Canvas 自绘烟花/鲜花粒子动画，不引入第三方库。

**备选方案**:
- Lottie 动画 → 需引入外部 JSON 动画文件，增加包体积
- Compose Animation API → Compose 内置，足够实现粒子效果

### 4. 排名展示时机

**决定**: 排名展示在计时**停止**时触发，多分组同时计时场景下需所有分组都停止才触发。

**单分组计时**: 用户只对一个分组计时，停止时立即计算并展示排名
**多分组同时计时**: 用户同时对多个分组计时（如两个分组同时运行），需等待所有分组都停止后，才触发排名展示，每个分组分别计算自己的排名

**备选方案**:
- 计时暂停就显示 → 干扰用户操作，且暂停后可能恢复继续
- App 打开就显示 → 打扰用户，削弱激励机制

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 烟花动画影响性能 | 使用 `remember` 缓存粒子状态，限制粒子数量 <= 50 |
| 奖品图片加载失败 | 提供默认占位图标（礼物盒 `ic_reward_placeholder.xml`），使用 `BitmapFactory.decodeFile()` 加载本地文件，失败时显示占位图 |
| 数据竞争（计时保存中计算排名） | 在 ViewModel 层确保先 `await()` 保存完成再计算 |

## Migration Plan

1. 在 `RecordDao` 添加 `countRecordsShorterThan(groupId: Long, duration: Long)` 方法，SQL: `SELECT COUNT(*) FROM records WHERE groupId = :groupId AND duration < :duration`
2. 在 `RecordRepository` 暴露 `countRecordsShorterThan()` 方法
3. 在 `PrizeDao` 添加 `getPrizesByGroupIdOnce(groupId: Long)` suspend 方法，在 `PrizeRepository` 暴露
4. `TimerViewModel` 添加 `calculateRanking()` 在计时停止时调用，使用 `PrizeDao.getPrizesByGroupIdOnce()` 获取奖品
5. 新增 `RewardDialog` Composable 组件实现弹窗和动效
6. `TimerPage` 集成 `RewardDialog`，在计时结束时触发

### 异步时序控制方案

当前 `TimerService.finishAndSave()` 在 `serviceScope.launch {}` 中异步保存记录，ViewModel 无法得知保存完成时机。为保证"先保存再排名"的时序：

**方案**: ViewModel 层负责记录保存和排名计算的时序控制。当 `stopTimer()`/`stopGroup()` 被调用时：

1. 获取当前时长和分组信息
2. 调用 Service 停止计时（Service 仍负责 `finishAndSave()` 保存记录）
3. ViewModel 在协程中调用 `countRecordsShorterThan()` 计算排名（此时 Service 已完成异步保存，因为 `stopTimer()` 调用是同步返回的，而 Service 的 `insertRecord` 在 `serviceScope` 中执行，通常在 ViewModel 后续协程执行前已完成）
4. 如需更严格的保证，可在 ViewModel 中额外调用 `delay(100)` 或改用 ViewModel 层直接保存记录并 `await()`

**Rollback**: 删除新增文件即可，不涉及数据库迁移。